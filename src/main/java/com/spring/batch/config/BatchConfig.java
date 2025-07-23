package com.spring.batch.config;

import javax.sql.DataSource;

import com.spring.batch.batch.processor.GenericTradeProcessor;
import com.spring.batch.batch.processor.TradeProcessorStrategy;
import com.spring.batch.batch.writer.GenericTradeWriter;
import com.spring.batch.batch.writer.TradeWriterStrategy;
import com.spring.batch.factory.TradeComponentFactory;
import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
import com.spring.batch.model.TradeDtoWrapper;
import com.spring.batch.service.KafkaProducerService;
import com.spring.batch.service.NcsFeedDataService;
import com.spring.batch.util.TradeUtil;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.utils.Exit;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final DataSource dataSource;
    private final FileMetadata fileMetadata;
    private final KafkaProducerService kafkaProducerService;
    private final NcsFeedDataService ncsFeedDataService;
    private final TradeComponentFactory tradeComponentFactory;

    public <T extends BaseTradeDto> Step processStep(JobRepository jobRepository,
                                                     PlatformTransactionManager transactionManager,
                                                     String tradeType) {

        ItemReader<T> reader = new LazyReader<>(tradeComponentFactory, tradeType); // ✅ Lazy wrapper
        ItemProcessor<T, TradeDtoWrapper<T>> processor = tradeComponentFactory.getProcessor(tradeType);
        ItemWriter<TradeDtoWrapper<T>> writer = tradeComponentFactory.getWriter(tradeType);

        return new StepBuilder("tradeBatchStep-" + tradeType, jobRepository)
                .<T, TradeDtoWrapper<T>>chunk(500, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor())
                .listener(stepExecutionListener())
                .build();
    }

    public <T extends BaseTradeDto> Job processJob(JobRepository jobRepository,
                                                   PlatformTransactionManager transactionManager,
                                                   String tradeType) {
        return new JobBuilder("processTradeJob-" + tradeType, jobRepository)
                .start(processStep(jobRepository, transactionManager, tradeType))
                .build();
    }

    @Bean
    public JobRepository jobRepository(PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setTransactionManager(transactionManager);
        factory.setDataSource(dataSource);
        factory.setDatabaseType("H2"); // or "oracle", "mysql", etc.
        factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
        factory.setTablePrefix("BATCH_");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cores = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cores * 2);
        executor.setMaxPoolSize(cores * 4);
        executor.setQueueCapacity(5000);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("batch-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setThreadPriority(Thread.NORM_PRIORITY);
        executor.initialize();
        return executor;
    }

    private StepExecutionListener stepExecutionListener() {
        return new StepExecutionListener() {
            public ExitStatus afterStep(StepExecution stepExecution) {
                boolean hasError = false;
                try {
                    List<String> kafkaMessages = TradeUtil.prepareMetadata(fileMetadata);
                    for (String kafkaMsg : kafkaMessages) {
                        kafkaProducerService.sendMessage(kafkaMsg);
                    }
                    ncsFeedDataService.persistExcepMarketData();
                    ncsFeedDataService.persistFileLinking(fileMetadata);
                } catch (Exception e) {
                    hasError = true;
                    e.printStackTrace();
                }

                boolean finalHasError = hasError;
                fileMetadata.getClientBatchIds().forEach((clientName, batchId) -> {
                    int recordCount = fileMetadata.getClientRecordCounts().getOrDefault(clientName, 0);

                    boolean hasProcessedRecords = recordCount > 0;
                    boolean isFailed = finalHasError || !hasProcessedRecords;
                    String status = isFailed ? "FAILED" : "COMPLETED";
                    //String status = finalHasError ? "FAILED" : "COMPLETED";
                    String des = finalHasError
                            ? "Trade file process failed for client "
                            : "Trade file process completed for client ";
                    ncsFeedDataService.updateTrackerStatus(batchId, status, des + clientName);
                });

                return ExitStatus.COMPLETED;
            }
        };
}


   /* @Bean(name = "gdsTradeWriter")
    @StepScope
    public GenericTradeWriter<TradeDto> gdsTradeWriter(
            FileMetadata fileMetadata,
            NcsFeedDataService tradeService,
            @Qualifier("gdsTradeWriterStrategy") TradeWriterStrategy<TradeDto> writerStrategy
    ) {
        return new GenericTradeWriter<>(fileMetadata, tradeService, writerStrategy);
    }

    @Bean(name = "gdsTradeProcessor")
    @StepScope
    public GenericTradeProcessor<TradeDto> gdsTradeProcessor(
            FileMetadata fileMetadata,
            NcsFeedDataService tradeService,
            @Qualifier("gdsTradeProcessorStrategy") TradeProcessorStrategy<TradeDto> processorStrategy
    ) {
        return new GenericTradeProcessor<>(fileMetadata, tradeService, processorStrategy);
    }*/


    /*@Bean(name = "ckjTradeProcessor")
    @StepScope
    public GenericTradeProcessor<CkjTradeDto> ckjTradeProcessor(
            FileMetadata fileMetadata,
            NcsFeedDataService tradeService,
            @Qualifier("ckjTradeProcessorStrategy") TradeProcessorStrategy<CkjTradeDto> processorStrategy
    ) {
        return new GenericTradeProcessor<>(fileMetadata, tradeService, processorStrategy);
    }

    @Bean(name = "ckjTradeWriter")
    @StepScope
    public GenericTradeWriter<CkjTradeDto> ckjTradeWriter(
            FileMetadata fileMetadata,
            NcsFeedDataService tradeService,
            @Qualifier("ckjTradeWriterStrategy") TradeWriterStrategy<CkjTradeDto> writerStrategy
    ) {
        return new GenericTradeWriter<>(fileMetadata, tradeService, writerStrategy);
    }*/
}



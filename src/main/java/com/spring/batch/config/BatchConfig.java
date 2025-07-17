package com.spring.batch.config;

import javax.sql.DataSource;

import com.spring.batch.factory.TradeComponentFactory;
import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.TradeDtoWrapper;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.*;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.core.task.*;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final DataSource dataSource;
    /*private final FileMetadata fileMetadata;
    private final KafkaProducerService kafkaProducerService;
    private final NcsFeedDataService ncsFeedDataService;;*/
    private final TradeComponentFactory tradeComponentFactory;

    // Declares a step dynamically based on tradeType
    public <T extends BaseTradeDto> Step processStep(JobRepository jobRepository,
                                                     PlatformTransactionManager transactionManager,
                                                     String tradeType) {

        ItemReader<T> itemReader = tradeComponentFactory.getReader(tradeType);
        ItemProcessor<T, TradeDtoWrapper<T>> itemProcessor = tradeComponentFactory.getProcessor(tradeType);
        ItemWriter<TradeDtoWrapper<T>> itemWriter = tradeComponentFactory.getWriter(tradeType);

        return new StepBuilder("tradeBatchStep-" + tradeType, jobRepository)
                .<T, TradeDtoWrapper<T>>chunk(500, transactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .taskExecutor(taskExecutor())
                .listener(stepExecutionListener())
                .build();
    }

    // Build job dynamically by passing tradeType
    public <T extends BaseTradeDto> Job processJob(JobRepository jobRepository,
                                                   PlatformTransactionManager transactionManager,
                                                   String tradeType) {

        return new JobBuilder("processTradeJob-" + tradeType, jobRepository)
                .start(processStep(jobRepository, transactionManager, tradeType))
                .build();
    }

    @Bean
    public JobRepository jobRepository(PlatformTransactionManager transactionManager) throws Exception {
        var factory = new JobRepositoryFactoryBean();
        factory.setTransactionManager(transactionManager);
        factory.setDataSource(dataSource);
        factory.setDatabaseType("H2");
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
        var executor = new ThreadPoolTaskExecutor();
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
           @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                boolean hasError = false;
                try {
                    /*List<String> kafkaMessages = TradeUtil.prepareMetadata(fileMetadata);
                    for (String kafkaMsg : kafkaMessages) {
                        kafkaProducerService.sendMessage(kafkaMsg);
                    }
                    ncsFeedDataService.persistExcepMarketData();
                    ncsFeedDataService.persistFileLinking(fileMetadata);*/

                } catch (Exception e) {
                    e.printStackTrace();
                    hasError = true;
                }

                /*boolean finalHasError = hasError;
                fileMetadata.getClientBatchIds().forEach((clientName, batchId) -> {
                    String status = finalHasError ? "FAILED" : "COMPLETED";
                    String des = finalHasError ? "Trade file process failed for client " : "Trade file process completed for client ";
                    ncsFeedDataService.updateTrackerStatus(batchId, status, des + clientName);
                });*/
                return ExitStatus.COMPLETED;
            }
        };
    }
}


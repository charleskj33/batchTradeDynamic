package com.spring.batch.listener;

import com.spring.batch.config.BatchConfig;
import com.spring.batch.dto.KafkaMessage;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.util.TradeConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenericBatchService {
    private final BatchConfig batchConfig;
    private final JobLauncher jobLauncher;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FileMetadata fileMetadata;
    private final TradeConfigProperties configProperties;

    public void triggerBatch(KafkaMessage kafkaMessage) {
        String type =mapAidToType(kafkaMessage.getAId());
        if(type ==null){
            return;
        }
        TradeConfigProperties.BlobConfig config= configProperties.getByTradeType(type);
        try {
            String containerName = config.getContainerName();
            String blobName = config.getBlobName();
            fileMetadata.reset();
            fileMetadata.setSourceSystem(type);

            Job job = batchConfig.processJob(jobRepository, transactionManager, type);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("tradeType", type)
                    .addString("containerName", containerName )   // Pass containerName here
                    .addString("blobName", blobName)             // Pass blobName here
                    .addLong("timeStamp", System.currentTimeMillis()) // For uniqueness
                    .toJobParameters();

            jobLauncher.run(job, jobParameters);

        } catch (Exception e) {
            log.error("Unexpected error running batch job", e);
        } finally {
            log.info("Batch execution attempted");
        }
    }

    private String mapAidToType(int aid) {
        try {
            return configProperties.getTradeTypeByAid(aid);
        } catch (IllegalArgumentException e) {
            log.warn("No trade type mapping found for AID: {}", aid);
            return null;
        }
    }
}

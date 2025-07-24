package com.spring.batch;

import com.spring.batch.config.BatchConfig;
import com.spring.batch.model.FileMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/*@Component
@RequiredArgsConstructor
public class ScheduledBatchJob {

    private final JobLauncher jobLauncher;
    private final BatchConfig batchConfig; // your dynamic job builder
    private final FileMetadata fileMetadata;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // Example: Run this every hour
    @Scheduled(fixedRate = 3600000)
    public void runJob() throws Exception {
        // Example trade types – you can read from DB/config/enum
        List<String> tradeTypes = List.of("gds"*//*, "ckj"*//*);

            //fileMetadata.reset(); // reset for each trade type

            Job job = batchConfig.processJob(jobRepository, transactionManager, "gds");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("tradeType", "gds")
                    .toJobParameters();

            jobLauncher.run(job, jobParameters);
        }
    }*/


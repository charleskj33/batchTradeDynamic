package com.spring.batch.Controller;

import com.spring.batch.config.BatchConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequiredArgsConstructor
public class JobTriggerController {

    private final JobLauncher jobLauncher;
    private final BatchConfig batchConfig;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @PostMapping("/run-job/{tradeType}")
    public ResponseEntity<String> runJob(@PathVariable String tradeType,
                                         @RequestParam String filePath) {
        try {
           /* // Validate input file exists
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.badRequest().body("File not found at path: " + filePath);
            }*/

            // Build job dynamically for given tradeType
            Job job = batchConfig.processJob(jobRepository, transactionManager, tradeType);

            // Add JobParameters
            JobParameters params = new JobParametersBuilder()
                    .addString("tradeType", tradeType)
                    .addString("filePath", filePath)
                    .addLong("startAt", System.currentTimeMillis()) // Ensures uniqueness
                    .toJobParameters();

            // Launch job
            jobLauncher.run(job, params);

            return ResponseEntity.ok("Job for " + tradeType + " started with filePath: " + filePath);
        } catch (JobExecutionAlreadyRunningException |
                 JobRestartException |
                 JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to launch job for " + tradeType + ": " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + e.getMessage());
        }
    }

}

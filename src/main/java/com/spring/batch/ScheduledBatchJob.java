package com.spring.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*@Component
public class ScheduledBatchJob {
    private final JobLauncher jobLauncher;
    private final Job processJob;


    public ScheduledBatchJob(JobLauncher jobLauncher, Job processJob) {
        this.jobLauncher = jobLauncher;
        this.processJob = processJob;
    }


    @Scheduled(fixedRate = 3600000)
    public void runJob() throws Exception{
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timeStamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(processJob, jobParameters);
    }
}*/

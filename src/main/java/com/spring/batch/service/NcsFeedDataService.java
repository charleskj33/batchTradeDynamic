package com.spring.batch.service;

import com.spring.batch.entity.ExceptionEntity;
import com.spring.batch.model.TrackerEntity;
import com.spring.batch.repository.TrackerRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class NcsFeedDataService {
    private final TrackerRepo trackerRepo;
    private final JavaMailSender javaMailSender;
    private final List<ExceptionEntity> exceptionEntityList = new ArrayList<>();
    public NcsFeedDataService(TrackerRepo trackerRepo, JavaMailSender javaMailSender) {
        this.trackerRepo = trackerRepo;
        this.javaMailSender = javaMailSender;
    }

    public void publishTracker(String batchId, String serviceName, String status, String description) {
        if (trackerRepo == null) {
            log.error("Failed to save");
            return;
        }

        try {
            trackerRepo.save(generateEntity(batchId, serviceName, status, description));
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    TrackerEntity generateEntity(String batchId, String serviceName, String status, String desc) {
        TrackerEntity te = new TrackerEntity();
        te.setBatchId(batchId);
        te.setService(serviceName);
        te.setStatus(status);
        te.setDesc(desc);
        te.setInsertedTimeStamp(LocalDateTime.now());
        te.setUpdatedTimeStamp(LocalDateTime.now());

        return te;
    }

    public void sendEmailWithAttachment() throws MessagingException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo("ckj1@ntrs.com");
            helper.setSubject("Action Needed: Invalid Data");
            helper.setText("unable to proceed with invalid data");
            javaMailSender.send(message);
        }catch (MessagingException e){
            e.printStackTrace();
        }
    }

    public void addExcepAdd(ExceptionEntity excep) {
        try{
            exceptionEntityList.add(excep);
        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }
}

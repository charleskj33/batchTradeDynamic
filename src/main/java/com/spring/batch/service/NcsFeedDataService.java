package com.spring.batch.service;

import com.spring.batch.entity.ExceptionEntity;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TrackerEntity;
import com.spring.batch.model.TradeFeedMasterEntity;
import com.spring.batch.repository.TrackerRepo;
import com.spring.batch.repository.TradeMasterRepo;
import com.spring.batch.repository.TradeRepository;
import com.spring.batch.repository.TradeRepositoryException;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NcsFeedDataService {
    private final TrackerRepo trackerRepo;
    private final JavaMailSender javaMailSender;
    private final List<ExceptionEntity> exceptionEntityList = new ArrayList<>();
    private final TradeRepository tradeRepository;
    private final TradeMasterRepo tradeMasterRepo;
    private final TradeRepositoryException tradeRepositoryException;
    @PersistenceContext
    private EntityManager entityManager;
    public NcsFeedDataService(TrackerRepo trackerRepo, JavaMailSender javaMailSender, TradeRepository tradeRepository, TradeMasterRepo tradeMasterRepo, TradeRepositoryException tradeRepositoryException) {
        this.trackerRepo = trackerRepo;
        this.javaMailSender = javaMailSender;
        this.tradeRepository = tradeRepository;
        this.tradeMasterRepo = tradeMasterRepo;
        this.tradeRepositoryException = tradeRepositoryException;
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

    public String determineMode(String clientName){
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        boolean existsToday = tradeRepository.existsForCLientToday(clientName, "trades", startOfDay, endOfDay);

        return existsToday ? "Delta" : "Flush";
    }

    @Transactional()
    public void persistExcepMarketData() {
        try {
            List<List<ExceptionEntity>> batches = new ArrayList<>();
            for(int i =0 ; i<exceptionEntityList.size();i+=1000){
                int endIndex = Math.min(i + 1000, exceptionEntityList.size());
                batches.add(new ArrayList<>(exceptionEntityList.subList(i, endIndex)));
            }

            for(List<ExceptionEntity> batch : batches){
               tradeRepositoryException.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void persistFileLinking(FileMetadata fileMetadata) {
        try {
            List<TradeFeedMasterEntity> links = fileMetadata.getClientBatchIds().entrySet().stream()
                    .map(entry -> TradeFeedMasterEntity.builder()
                            .fileId(fileMetadata.getBatchId())
                            .clientName(entry.getKey())
                            .batchId(entry.getValue())
                            .sourceSystem(fileMetadata.getSourceSystem())
                            .totalMsg(fileMetadata.getClientRecordCount(entry.getKey()))
                            .insertedTimeStamp(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());

            tradeMasterRepo.saveAll(links);
        } catch (Exception e) {
            log.error("Error saving file linking records", e);
            throw new RuntimeException("Error persisting file linking data", e);
        }
    }

    public void updateTrackerStatus(String batchId, String status, String description){

        if(StringUtils.isEmpty(batchId)){
            throw new RuntimeException();
        }

        trackerRepo.findByBatchId(batchId).ifPresentOrElse(tracker -> {
            tracker.setStatus(status);
            tracker.setDesc(description);
            tracker.setUpdatedTimeStamp(LocalDateTime.now());
            trackerRepo.save(tracker);
        }, () -> {
            log.warn("No Tracker found for batchId {}. Skipping update.", batchId);
        });
    }
}

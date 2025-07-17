package com.spring.batch.batch.reader;

import com.spring.batch.entity.ExceptionEntity;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;

import com.spring.batch.repository.ClientRepo;
import com.spring.batch.repository.PrincipalRepo;
import com.spring.batch.service.NcsFeedDataService;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import com.spring.batch.entity.Principals;
import com.spring.batch.entity.Clients;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component("gdsTradeReader")
public class gdsTradeReader implements ItemReader<TradeDto> {

    private final FileMetadata fileMetadata;
    private final NcsFeedDataService tradeService;
    private final Iterator<TradeDto> iterator;
    private final PrincipalRepo principalRepo;
    private final ClientRepo clientRepo;

    public gdsTradeReader(FlatFileItemReader<TradeDto> delegate,
                          FileMetadata fileMetadata,
                          NcsFeedDataService tradeService,
                          PrincipalRepo principalRepo,
                          ClientRepo clientRepo) throws Exception {

        this.fileMetadata = fileMetadata;
        this.tradeService = tradeService;
        this.principalRepo = principalRepo;
        this.clientRepo = clientRepo;

        List<TradeDto> tradeDtoList = readRecords(delegate);
        if (shouldSkip(tradeDtoList)) {
            handleValidationFailure();
            this.iterator = Collections.emptyIterator();
            return;
        }

        Map<String, TradeDto> deduped = filterAndDeduplicate(tradeDtoList);
        List<TradeDto> validTrades = updateClientBatchIds(deduped.values());
        fileMetadata.setAllRecords(validTrades.stream().map(TradeDto::getTradeRef).toList());
        this.iterator = validTrades.iterator();
    }

    @Override
    public TradeDto read() {
        return (iterator != null && iterator.hasNext()) ? iterator.next() : null;
    }

    private void handleValidationFailure() throws MessagingException {
        String errMsg ="unable to proceed because one or more columns";
        feedDataExcepEntity(fileMetadata.getBatchId(), tradeService, "", errMsg);
        tradeService.publishTracker(fileMetadata.getBatchId(), "FILE-PROCESSING", "NOT_STARTED", "Gds File Process not started");
        //tradeService.sendEmailWithAttachment();
    }

    private List<TradeDto> readRecords(FlatFileItemReader<TradeDto> delegate) throws Exception {
        List<TradeDto> tradeDtoList = new ArrayList<>();
        delegate.open(new ExecutionContext());

        try {
            TradeDto item;
            while ((item = delegate.read()) != null) {
                tradeDtoList.add(item);
            }
        } catch (FlatFileParseException e) {
            log.error("Field count mismatch: {}", e.getMessage());
        } finally {
            delegate.close();
        }

        return tradeDtoList;
    }

    private boolean shouldSkip(List<TradeDto> records) {
        boolean hasMissingPrincipal = records.stream().anyMatch(record -> isNullOrEmpty(record.getPrincipal()));
        boolean hasNonZeroMtm = records.stream().anyMatch(record -> !isZeroOrEmptyMtm(record.getMtmValuation()));
        boolean hasMissingMtmDate = records.stream().anyMatch(record -> isNullOrEmpty(record.getMtmValuationDate()));

        return !hasMissingPrincipal || !hasNonZeroMtm || !hasMissingMtmDate;
    }

    private Map<String, TradeDto> filterAndDeduplicate(List<TradeDto> records) {
        Map<String, TradeDto> uniqueList = new HashMap<>();

        for (TradeDto record : records) {
            if (!isNullOrEmpty(record.getTradeRef())) {
                uniqueList.put(generateId(), record);
                continue;
            }

            TradeDto existing = uniqueList.putIfAbsent(record.getTradeRef(), record);
            if(existing !=null){
                validateRecordSkipDuplicate(record, uniqueList);
            }

        }
        return uniqueList;
    }

    private boolean isNullOrEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isZeroOrEmptyMtm(String value) {
        if (value == null || value.trim().isEmpty())
            return true;

        try {
            BigDecimal bd = new BigDecimal(value);
            return bd.compareTo(BigDecimal.ZERO) == 0;
        } catch (NumberFormatException e) {
            log.warn("Invalid MTM value: {}", value);
            return true;
        }
    }

    public void validateRecordSkipDuplicate(TradeDto newRecord, Map<String, TradeDto> uniRecords) {
        String newRecordTradeRef = newRecord.getTradeRef();
        String errMsg = "";

        TradeDto existingRec = uniRecords.get(newRecordTradeRef);
        String existingRecTradeRef = existingRec.getTradeRef();

        BigDecimal newMtmVal = parseMtmValue(newRecord.getMtmValuation());
        BigDecimal existingMtmVal = parseMtmValue(existingRec.getMtmValuation());
        String batchId = StringUtils.isNotEmpty(fileMetadata.getBatchId()) ? fileMetadata.getBatchId() : generateId();

        // Case 1: Both have zero MtM
        if (isZero(newMtmVal) && isZero(existingMtmVal)) {
            errMsg = "Trade not sent to CM. Failed validation. MtValuation is zero for both existing and new record.";
            log.info(errMsg);
            uniRecords.put(newRecordTradeRef + "_" + generateId(), newRecord);
            return;
        }

        // Case 2: MtmValDate is missing
        if (!isNullOrEmpty(newRecord.getMtmValuationDate())) {
            errMsg = "Trade not sent to CM. Failed validation. MtmValDate is empty for new record.";
            log.info(errMsg);
            feedDataExcepEntity(batchId, tradeService, newRecordTradeRef, errMsg);
            return;
        }

        if (!isNullOrEmpty(existingRec.getMtmValuationDate())) {
            errMsg = "Trade not sent to CM. Failed validation. MtmValDate is empty for existing record.";
            log.info(errMsg);
            feedDataExcepEntity(batchId, tradeService, existingRecTradeRef, errMsg);
            uniRecords.put(newRecordTradeRef, newRecord); // Prefer new record
            return;
        }

        // Case 3: Compare dates and values
        LocalDate newValDate = convertStringToDate(newRecord.getMtmValuationDate());
        LocalDate existingValDate = convertStringToDate(existingRec.getMtmValuationDate());

        if (!newValDate.isBefore(existingValDate)) {
            int mtmComparison = newMtmVal.compareTo(existingMtmVal);

            if (mtmComparison == 0) {
                // Same MtM, keep either – here we keep the existing
                errMsg = " TradeRef, mtmVal, mtmval date same value ignore this trade";
                feedDataExcepEntity(batchId, tradeService, newRecordTradeRef, errMsg);
                return;
            }

            if (!isZero(newMtmVal) && isZero(existingMtmVal)) {
                // Prefer new record
                log.info("Replacing existing zero MTM with new non-zero MTM");
                feedDataExcepEntity(batchId, tradeService, existingRecTradeRef, "Replaced by new record with valid MTM");
                uniRecords.put(newRecordTradeRef, newRecord);
            } else if (isZero(newMtmVal) && !isZero(existingMtmVal)) {
                log.info("New record has zero MTM, keeping existing");
                feedDataExcepEntity(batchId, tradeService, existingRecTradeRef, "New record has zero MTM");
            } else {
                // Both are non-zero and different – treat both as exceptions
                log.info("Both records have non-zero but different MTM values. add both.");
                uniRecords.put(newRecordTradeRef + "_" + generateId(), newRecord);
            }
        } else {
            uniRecords.put(newRecordTradeRef + "_" + generateId(), newRecord);
        }
    }

    private BigDecimal parseMtmValue(String val) {
        try {
            return new BigDecimal(val.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean isZero(BigDecimal val) {
        return val == null || val.compareTo(BigDecimal.ZERO) == 0;
    }

    private LocalDate convertStringToDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateStr, formatter);
    }

    private static void feedDataExcepEntity(String batchId, NcsFeedDataService ncsFeedDataService, String tradeRef, String errMsg){
        ExceptionEntity entity = new ExceptionEntity();
        entity.setBatchId(batchId);
        entity.setTradeRef(tradeRef);
        entity.setErrorMessage(errMsg);
        ncsFeedDataService.addExcepAdd(entity);
    }

    public String generateId(){
        return UUID.randomUUID().toString();
    }

    private List<TradeDto> updateClientBatchIds(Collection<TradeDto> tradeDtos) {
        Set<String> processedClientIds = new HashSet<>();
        List<TradeDto> gdsDtos = new ArrayList<>();

        for (TradeDto dto : tradeDtos) {
            String pName = dto.getPrincipal();
            Optional<String> clientNameOpt = findClientNameByPrincipal(pName);

            if (clientNameOpt.isPresent()) {
                String clientName = clientNameOpt.get();
                if (processedClientIds.add(clientName)) {
                    String clientBatchId = fileMetadata.getBatchIdForClient(clientName);
                    tradeService.publishTracker(clientBatchId, "File Processing", "InProgress", "file Processing for Client" + clientName);
                }
                gdsDtos.add(dto);
            }
            else{
                feedDataExcepEntity(fileMetadata.getBatchId(), tradeService, dto.getTradeRef(), "Invalid Principal or client");
            }
        }
        return gdsDtos;
    }

    private Optional<String> findClientNameByPrincipal(String pName){
        return Optional.ofNullable(pName)
                .filter(StringUtils :: isNotBlank)
                .flatMap(principalName -> principalRepo.findByPcmByName(principalName)
                        .map(Principals::getClientCmId)
                        .flatMap(clientRepo :: findByShortName))
                .map(Clients::getShortName)
                .filter(StringUtils :: isNotBlank);
    }
}



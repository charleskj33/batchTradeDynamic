package com.spring.batch.batch.reader;

import ch.qos.logback.core.net.server.Client;
import com.spring.batch.entity.Clients;
import com.spring.batch.entity.ExceptionEntity;
import com.spring.batch.entity.Principals;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
import com.spring.batch.repository.ClientRepo;
import com.spring.batch.repository.PrincipalRepo;
import com.spring.batch.service.NcsFeedDataService;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component("gdsTradeReaderStrategy")
@RequiredArgsConstructor
public class GdsTradeReaderStrategy implements TradeReaderStrategy<TradeDto> {

    private final NcsFeedDataService tradeService;
    private final PrincipalRepo principalRepo;
    private final ClientRepo clientRepo;

    @Override
    public boolean shouldSkip(List<TradeDto> records) {
        return records.stream().anyMatch(record ->
                isNullOrEmpty(record.getPrincipal()) ||
                        isNullOrEmpty(record.getMtmValuationDate()) ||
                        !isZeroOrEmptyMtm(record.getMtmValuation())
        );
    }

    @Override
    public void handleValidationFailure(FileMetadata fileMetadata, NcsFeedDataService ncsFeedDataService) throws MessagingException {
        String errMsg = "File validation failed due to required column issues";
        feedDataExcepEntity(fileMetadata.getBatchId(), tradeService, "", errMsg);
    }

    @Override
    public Map<String, TradeDto> filterAndDeDuplicate(List<TradeDto> records, FileMetadata fileMetadata, NcsFeedDataService ncsFeedDataService) {
        Map<String, TradeDto> result = new HashMap<>();

        for (TradeDto record : records) {
            if (isNullOrEmpty(record.getTradeRef())) {
                result.put(generateId(), record);
                continue;
            }

            TradeDto existing = result.putIfAbsent(record.getTradeRef(), record);
            if (existing != null) {
                validateDuplicate(record, existing, result, fileMetadata.getBatchId());
            }
        }

        return result;
    }

    @Override
    public List<TradeDto> enrichRecords(Collection<TradeDto> records, FileMetadata fileMetadata, NcsFeedDataService ncsFeedDataService) {
        Set<String> ids = new HashSet<>();
        List<TradeDto> tradeDtos = new ArrayList<>();

        for(TradeDto dto: records) {
            String pName = dto.getPrincipal();

            Optional<String> clientName = findClientPr(pName);

            if (clientName.isPresent()) {
                String cName = clientName.get();

                if (ids.add(cName)) {
                    String clientBatch = fileMetadata.getBatchIdForClient(cName);
                    ncsFeedDataService.publishTracker(clientBatch, "trade", "INPROGRESS", "started");
                }
                tradeDtos.add(dto);
            }else{
                feedDataExcepEntity(fileMetadata.getBatchId(), ncsFeedDataService, dto.getTradeRef(), "Invalid");
            }
        }

        return tradeDtos;
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

    private boolean isZeroOrEmptyMtm(String val) {
        return val == null || val.trim().isEmpty() || isZero(parseMtmValue(val));
    }

    private boolean isNullOrEmpty(String val) {
        return val == null || val.trim().isEmpty();
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }

    private void feedDataExcepEntity(String batchId, NcsFeedDataService tradeService, String tradeRef, String msg) {
        ExceptionEntity ex = new ExceptionEntity();
        ex.setBatchId(batchId);
        ex.setTradeRef(tradeRef);
        ex.setErrorMessage(msg);
        tradeService.addExcepAdd(ex);
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    private void validateDuplicate(TradeDto newRec, TradeDto existingRec, Map<String, TradeDto> map, String batchId) {
        String tradeRef = newRec.getTradeRef();
        BigDecimal newMtm = parseMtmValue(newRec.getMtmValuation());
        BigDecimal existingMtm = parseMtmValue(existingRec.getMtmValuation());

        LocalDate newDate = parseDate(newRec.getMtmValuationDate());
        LocalDate existingDate = parseDate(existingRec.getMtmValuationDate());

        if (isZero(newMtm) && isZero(existingMtm)) {
            map.put(tradeRef + "_" + generateId(), newRec);
            return;
        }

        if (isNullOrEmpty(newRec.getMtmValuationDate())) {
            feedDataExcepEntity(batchId, tradeService, tradeRef, "New record missing MTM date");
            return;
        }

        if (isNullOrEmpty(existingRec.getMtmValuationDate())) {
            feedDataExcepEntity(batchId, tradeService, tradeRef, "Existing record missing MTM date");
            map.put(tradeRef, newRec); // prefer new
            return;
        }

        if (!newDate.isBefore(existingDate)) {
            if (newMtm.compareTo(existingMtm) == 0) {
                feedDataExcepEntity(batchId, tradeService, tradeRef, "Duplicate record (same MTM and date)");
            } else if (!isZero(newMtm) && isZero(existingMtm)) {
                feedDataExcepEntity(batchId, tradeService, tradeRef, "Replaced existing zero MTM");
                map.put(tradeRef, newRec);
            } else {
                map.put(tradeRef + "_" + generateId(), newRec); // preserve both
            }
        } else {
            map.put(tradeRef + "_" + generateId(), newRec); // older one; keep both
        }
    }

    private Optional<String> findClientPr(String pName){
        return Optional.ofNullable(pName)
                .filter(StringUtils:: isNotBlank)
                .flatMap(principalName -> principalRepo.findByPcmByName(principalName)
                        .map(Principals::getClientCmId)
                        .flatMap(clientRepo :: findByShortName))
                .map(Clients::getShortName)
                .filter(StringUtils :: isNotBlank);
    }
}


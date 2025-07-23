package com.spring.batch.batch.processor;

import com.spring.batch.entity.Agreement;
import com.spring.batch.entity.Clients;
import com.spring.batch.entity.ExceptionEntity;
import com.spring.batch.entity.Principals;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
import com.spring.batch.model.TradeDtoWrapper;
import com.spring.batch.repository.AgreementRepo;
import com.spring.batch.repository.ClientRepo;
import com.spring.batch.repository.CounterPartyRepo;
import com.spring.batch.repository.PrincipalRepo;
import com.spring.batch.service.NcsFeedDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("gdsTradeProcessorStrategy")
@RequiredArgsConstructor
public class GdsTradeProcessorStrategy implements TradeProcessorStrategy<TradeDto> {

    private final PrincipalRepo principalRepo;
    private final CounterPartyRepo counterPartyRepo;
    private final AgreementRepo agreementRepo;
    private final ClientRepo clientRepo;

    @Override
    public TradeDtoWrapper<TradeDto> process(TradeDto item, FileMetadata metadata, NcsFeedDataService service) {
        String tradeRef = item.getTradeRef();

        try {
            String principalName = item.getPrincipal();
            String orgCode = item.getOrgCode();

            //return new TradeDtoWrapper<>(item, "CKJ", "1243");
            Principals principal = principalRepo.findByPcmByName(principalName).orElse(null);
            if (principal == null) {
                return handle(item, metadata, service, "Principal not found");
            }

            String clientCmId = principal.getClientCmId();
            String principalCmId = principal.getCmId();

            String clientName = Optional.ofNullable(clientCmId)
                    .flatMap(clientRepo::findByShortName)
                    .map(Clients::getShortName)
                    .orElse(null);

            String counterpartyId = counterPartyRepo.findByCountName(orgCode, clientCmId).orElse(null);

            if (counterpartyId == null) {
                return handle(item, metadata, service, "Counterparty not found");
            }

            String externalId = findExternalId(principalCmId, counterpartyId, item.getProduct(), item.getDealDate());

            if (clientName == null || externalId == null) {
                return handle(item, metadata, service, "Missing client name or externalId");
            }

            return new TradeDtoWrapper<>(item, clientName, externalId);

        } catch (Exception e) {
            log.error("Failed to process tradeRef {}: {}", tradeRef, e.getMessage(), e);
            return handle(item, metadata, service, "Unhandled processor error");
        }
    }

    private TradeDtoWrapper<TradeDto> handle(TradeDto item, FileMetadata metadata, NcsFeedDataService service, String error) {
        feedDataExcepEntity(metadata.getBatchId(), service, item.getTradeRef(), error);
        log.warn("Rejected tradeRef {}: {}", item.getTradeRef(), error);
        return null;
    }

    private static void feedDataExcepEntity(String batchId, NcsFeedDataService ncsFeedDataService, String tradeRef, String errMsg) {
        ExceptionEntity entity = new ExceptionEntity();
        entity.setBatchId(batchId);
        entity.setTradeRef(tradeRef);
        entity.setErrorMessage(errMsg);
        ncsFeedDataService.addExcepAdd(entity);
    }

    public String findExternalId(String principalCmId, String counterPartyCmId, String product, String dealDateStr) {
        if (StringUtils.isBlank(principalCmId) || StringUtils.isBlank(counterPartyCmId)) {
            return null;
        }

        List<Agreement> agreementList = agreementRepo.findByExternalId(principalCmId, counterPartyCmId);
        if (CollectionUtils.isEmpty(agreementList)) {
            log.info("No agreement found for CM IDs: {}, {}", principalCmId, counterPartyCmId);
            return null;
        }

      return handleMultipleAgreements(agreementList, product, dealDateStr);
    }

    private String handleMultipleAgreements(List<Agreement> agreements, String product, String dealDateStr) {

        // Determine if the product is RI or RO
        boolean isProductRIorRO = "RI".equalsIgnoreCase(product) || "RO".equalsIgnoreCase(product);

        // Filter based on product type and masterAgType
        List<Agreement> filteredAgreements = agreements.stream()
                .filter(agreement -> matchesProductAndMasterAgType(agreement, product, isProductRIorRO))
                .toList();

        // If no agreement matches, return null
        if (filteredAgreements.isEmpty()) {
            return null;
        }

        // If there's exactly one valid agreement, return its externalId (no date check needed)
        if (filteredAgreements.size() == 1) {
            return StringUtils.defaultIfBlank(filteredAgreements.get(0).getExternalId(), null);
        }

        // Parse the deal date string into a LocalDate
        Optional<LocalDate> dealDateOpt = parseDateSafe(dealDateStr);

        if (dealDateOpt.isEmpty()) {
            log.warn("Deal date is missing or invalid: {}", dealDateStr);
            return null;
        }

        // If multiple records match, apply the date filter first, then find the latest agreement by date
        return filteredAgreements.stream()
                // Filter based on the agreement date being valid (less than or equal to dealDate)
                .filter(agreement -> isAgreementDateValid(agreement, dealDateOpt.get()))  // Only valid agreements
                .max(Comparator.comparing(a -> parseDateSafe(a.getAgreeDate()).orElse(LocalDate.MIN)))  // Get the latest date
                .map(Agreement::getExternalId)
                .filter(StringUtils::isNotBlank) // Ensure the externalId is not blank
                .orElse(null); // If no valid externalId found, return null
    }

    // Helper method for matching product and masterAgType
    private boolean matchesProductAndMasterAgType(Agreement agreement, String product, boolean isProductRIorRO) {
        // Fetch the tradeTypes from the agreement
        String tradeTypes = agreement.getTrTypes();

        // If tradeTypes is blank or null, we don't process this agreement
        if (StringUtils.isBlank(tradeTypes)) {
            return false;
        }

        // Convert the product to uppercase for case-insensitive matching
        String pC = StringUtils.defaultString(product).toUpperCase();

        // Check if tradeTypes contains the product (case-insensitive)
        boolean isProductMatch = Arrays.stream(tradeTypes.split(","))
                .map(String::trim)  // Trim each trade type before matching
                .anyMatch(type -> StringUtils.containsIgnoreCase(type, pC));  // Case-insensitive match

        // If the product matches, proceed with masterAgType validation
        if (isProductMatch) {
            // Master agreement type check (optimize by checking once)
            String masterAgType = agreement.getMasterAgType();

            if (isProductRIorRO) {
                // If the product is RI or RO, ensure masterAgType contains "IS" or "GM"
                return masterAgType.contains("IS") || masterAgType.contains("GM");
            } else {
                // If the product is not RI or RO, ensure masterAgType does NOT contain "IS" or "GM"
                return !(masterAgType.contains("IS") || masterAgType.contains("GM"));
            }
        }

        // If product doesn't match, return false
        return false;
    }


    // Helper method to validate agreement date
    private boolean isAgreementDateValid(Agreement agreement, LocalDate dealDate) {
        Optional<LocalDate> agreementDateOpt = parseDateSafe(agreement.getAgreeDate());
        return agreementDateOpt.isPresent() && !agreementDateOpt.get().isAfter(dealDate);  // Ensure agreementDate <= dealDate
    }

    private Optional<LocalDate> parseDateSafe(String dateStr) {
        try {
            return Optional.ofNullable(dateStr)
                    .map(LocalDate::parse); // assumes ISO format (yyyy-MM-dd)
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", dateStr);
            return Optional.empty();
        }
    }
}


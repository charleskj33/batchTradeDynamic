package com.batch.test.processor;

import com.spring.batch.batch.processor.GdsTradeProcessorStrategy;
import com.spring.batch.entity.Agreement;
import com.spring.batch.repository.AgreementRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GdsTradeProcessorStrategyTest {

    private AgreementRepo agreementRepo;
    private GdsTradeProcessorStrategy strategy;

    @BeforeEach
    void setUp() {
        agreementRepo = mock(AgreementRepo.class);
        strategy = new GdsTradeProcessorStrategy(null, null, agreementRepo, null);
    }

    private Agreement createAgreement(String extId, String masterType, String trTypes, String agreeDate) {
        Agreement ag = new Agreement();
        ag.setExternalId(extId);
        ag.setMasterAgType(masterType);
        ag.setTrTypes(trTypes);
        ag.setAgreeDate(agreeDate);
        return ag;
    }

    @Test
    void test_ri_product_matches_is_master_type_and_date_valid() {
        Agreement ag = createAgreement("123", "ISDA", "RI,FX", "2024-05-01");
        when(agreementRepo.findByExternalId("P1", "C1")).thenReturn(List.of(ag));

        String externalId = strategy.findExternalId("P1", "C1", "RI", "2024-06-01");
        assertEquals("123", externalId);
    }

    @Test
    void test_fx_product_excludes_is_master_type() {
        Agreement ag = createAgreement("234", "ISDA", "FX", "2024-04-01");
        when(agreementRepo.findByExternalId("P1", "C1")).thenReturn(List.of(ag));

        String externalId = strategy.findExternalId("P1", "C1", "FX", "2024-05-01");
        assertNull(externalId);
    }

    @Test
    void test_fx_product_includes_valid_non_is_master_type() {
        Agreement ag = createAgreement("345", "CSA", "FX", "2024-04-01");
        when(agreementRepo.findByExternalId("P1", "C1")).thenReturn(List.of(ag));

        String externalId = strategy.findExternalId("P1", "C1", "FX", "2024-05-01");
        assertEquals("345", externalId);
    }

    @Test
    void test_ri_product_with_multiple_agreements_returns_latest_valid() {
        Agreement ag1 = createAgreement("A1", "ISDA", "RI", "2024-03-01");
        Agreement ag2 = createAgreement("A2", "ISDA", "RI", "2024-05-01");
        Agreement ag3 = createAgreement("A3", "ISDA", "RI", "2023-01-01"); // older

        when(agreementRepo.findByExternalId("P1", "C1")).thenReturn(List.of(ag1, ag2, ag3));

        String externalId = strategy.findExternalId("P1", "C1", "RI", "2024-06-01");
        assertEquals("A2", externalId);
    }

    @Test
    void test_ri_product_with_agreement_date_after_deal_date_returns_null() {
        Agreement ag = createAgreement("999", "ISDA", "RI", "2025-01-01");
        when(agreementRepo.findByExternalId("P1", "C1")).thenReturn(List.of(ag));

        String externalId = strategy.findExternalId("P1", "C1", "RI", "2024-06-01");
        assertNull(externalId);
    }

    @Test
    void test_agreement_missing_trTypes_should_be_skipped() {
        Agreement ag = createAgreement("999", "ISDA", null, "2024-01-01");
        when(agreementRepo.findByExternalId("P1", "C1")).thenReturn(List.of(ag));

        String externalId = strategy.findExternalId("P1", "C1", "RI", "2024-06-01");
        assertNull(externalId);
    }

    @Test
    void test_agreement_with_blank_externalId_returns_null() {
        Agreement ag = createAgreement("", "ISDA", "RI", "2024-01-01");
        when(agreementRepo.findByExternalId("P1", "C1")).thenReturn(List.of(ag));

        String externalId = strategy.findExternalId("P1", "C1", "RI", "2024-06-01");
        assertNull(externalId);
    }
}


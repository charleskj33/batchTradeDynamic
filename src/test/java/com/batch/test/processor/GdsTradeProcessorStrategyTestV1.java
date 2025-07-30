package com.batch.test.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.spring.batch.batch.processor.GdsTradeProcessorStrategy;
import com.spring.batch.entity.*;
import com.spring.batch.model.*;
import com.spring.batch.repository.*;
import com.spring.batch.service.NcsFeedDataService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.util.*;

class GdsTradeProcessorStrategyTestV1 {

    @Mock
    private PrincipalRepo principalRepo;
    @Mock
    private CounterPartyRepo counterPartyRepo;
    @Mock
    private AgreementRepo agreementRepo;
    @Mock
    private ClientRepo clientRepo;
    @Mock
    private NcsFeedDataService ncsFeedDataService;

    private GdsTradeProcessorStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new GdsTradeProcessorStrategy(principalRepo, counterPartyRepo, agreementRepo, clientRepo);
    }

    @Test
    void testProcess_successful() {
        TradeDto trade = new TradeDto();
        trade.setTradeRef("T123");
        trade.setPrincipal("Principal1");
        trade.setOrgCode("Org1");
        trade.setProduct("RI");
        trade.setDealDate("2023-05-10");

        Principals principal = new Principals();
        principal.setClientCmId("ClientCmId1");
        principal.setCmId("PrincipalCmId1");

        Clients client = new Clients();
        client.setShortName("ClientShortName");

        String counterPartyId = "CounterPartyId1";

        Agreement agreement = new Agreement();
        agreement.setExternalId("ExternalId1");
        agreement.setTrTypes("RI,RO");
        agreement.setMasterAgType("IS");
        agreement.setAgreeDate("2023-01-01");

        // Mocks
        when(principalRepo.findByPcmByName("Principal1")).thenReturn(Optional.of(principal));
        when(clientRepo.findByShortName("ClientCmId1")).thenReturn(Optional.of(client));
        when(counterPartyRepo.findByCountName("Org1", "ClientCmId1")).thenReturn(Optional.of(counterPartyId));
        when(agreementRepo.findByExternalId("PrincipalCmId1", counterPartyId)).thenReturn(List.of(agreement));

        TradeDtoWrapper<TradeDto> result = strategy.process(trade, new FileMetadata(), ncsFeedDataService);

        assertNotNull(result);
        assertEquals("ClientShortName", result.getClientName());
        assertEquals("ExternalId1", result.getExternalId());

        // Verify no exceptions logged
        verifyNoInteractions(ncsFeedDataService);
    }

    @Test
    void testProcess_principalNotFound() {
        TradeDto trade = new TradeDto();
        trade.setTradeRef("T123");
        trade.setPrincipal("UnknownPrincipal");

        when(principalRepo.findByPcmByName("UnknownPrincipal")).thenReturn(Optional.empty());

        TradeDtoWrapper<TradeDto> result = strategy.process(trade, new FileMetadata(), ncsFeedDataService);

        assertNull(result);
        verify(ncsFeedDataService).addExcepAdd(argThat(entity ->
                entity.getTradeRef().equals("T123") && entity.getErrorMessage().contains("Principal not found")));
    }

    @Test
    void testProcess_clientNameNotFound() {
        TradeDto trade = new TradeDto();
        trade.setTradeRef("T123");
        trade.setPrincipal("Principal1");

        Principals principal = new Principals();
        principal.setClientCmId("ClientCmId1");

        when(principalRepo.findByPcmByName("Principal1")).thenReturn(Optional.of(principal));
        when(clientRepo.findByShortName("ClientCmId1")).thenReturn(Optional.empty());

        TradeDtoWrapper<TradeDto> result = strategy.process(trade, new FileMetadata(), ncsFeedDataService);

        assertNull(result);
        verify(ncsFeedDataService).addExcepAdd(argThat(entity ->
                entity.getTradeRef().equals("T123") && entity.getErrorMessage().contains("clientName not found")));
    }

    @Test
    void testProcess_counterpartyNotFound() {
        TradeDto trade = new TradeDto();
        trade.setTradeRef("T123");
        trade.setPrincipal("Principal1");
        trade.setOrgCode("Org1");

        Principals principal = new Principals();
        principal.setClientCmId("ClientCmId1");

        Clients client = new Clients();
        client.setShortName("ClientShortName");

        when(principalRepo.findByPcmByName("Principal1")).thenReturn(Optional.of(principal));
        when(clientRepo.findByShortName("ClientCmId1")).thenReturn(Optional.of(client));
        when(counterPartyRepo.findByCountName("Org1", "ClientCmId1")).thenReturn(Optional.empty());

        TradeDtoWrapper<TradeDto> result = strategy.process(trade, new FileMetadata(), ncsFeedDataService);

        assertNull(result);
        verify(ncsFeedDataService).addExcepAdd(argThat(entity ->
                entity.getTradeRef().equals("T123") && entity.getErrorMessage().contains("Counterparty not found")));
    }

    @Test
    void testProcess_externalIdNotFound() {
        TradeDto trade = new TradeDto();
        trade.setTradeRef("T123");
        trade.setPrincipal("Principal1");
        trade.setOrgCode("Org1");
        trade.setProduct("RI");
        trade.setDealDate("2023-05-10");

        Principals principal = new Principals();
        principal.setClientCmId("ClientCmId1");
        principal.setCmId("PrincipalCmId1");

        Clients client = new Clients();
        client.setShortName("ClientShortName");

        String counterPartyId = "CounterPartyId1";

        when(principalRepo.findByPcmByName("Principal1")).thenReturn(Optional.of(principal));
        when(clientRepo.findByShortName("ClientCmId1")).thenReturn(Optional.of(client));
        when(counterPartyRepo.findByCountName("Org1", "ClientCmId1")).thenReturn(Optional.of(counterPartyId));
        when(agreementRepo.findByExternalId("PrincipalCmId1", counterPartyId)).thenReturn(Collections.emptyList());

        TradeDtoWrapper<TradeDto> result = strategy.process(trade, new FileMetadata(), ncsFeedDataService);

        assertNull(result);
        verify(ncsFeedDataService).addExcepAdd(argThat(entity ->
                entity.getTradeRef().equals("T123") && entity.getErrorMessage().contains("Missing client name or externalId")));
    }

    @Test
    void testHandleMultipleAgreements_multipleFilteredByDate() {
        Agreement ag1 = new Agreement();
        ag1.setExternalId("E1");
        ag1.setTrTypes("RI");
        ag1.setMasterAgType("IS");
        ag1.setAgreeDate("2023-01-01");

        Agreement ag2 = new Agreement();
        ag2.setExternalId("E2");
        ag2.setTrTypes("RI");
        ag2.setMasterAgType("IS");
        ag2.setAgreeDate("2023-02-01");

        List<Agreement> agreements = List.of(ag1, ag2);

        String externalId = strategy.handleMultipleAgreements(agreements, "RI", "2023-02-15");

        assertEquals("E2", externalId);
    }

    @Test
    void testHandleMultipleAgreements_invalidDate() {
        Agreement ag = new Agreement();
        ag.setExternalId("E1");
        ag.setTrTypes("RI");
        ag.setMasterAgType("IS");
        ag.setAgreeDate("invalid-date");

        List<Agreement> agreements = List.of(ag);

        String externalId = strategy.handleMultipleAgreements(agreements, "RI", "2023-02-15");

        // Because the date parsing fails, it returns null
        assertNull(externalId);
    }
}


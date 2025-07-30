package com.batch.test.reader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.spring.batch.batch.reader.GdsTradeReaderStrategy;
import com.spring.batch.entity.Clients;
import com.spring.batch.entity.Principals;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
import com.spring.batch.service.NcsFeedDataService;
import com.spring.batch.repository.ClientRepo;
import com.spring.batch.repository.PrincipalRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

class GdsTradeReaderStrategyTest {

    @InjectMocks
    private GdsTradeReaderStrategy strategy;

    @Mock
    private NcsFeedDataService tradeService;

    @Mock
    private PrincipalRepo principalRepo;

    @Mock
    private ClientRepo clientRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testShouldSkip() {
        // Create valid and invalid records
        TradeDto validTrade = new TradeDto();
        validTrade.setPrincipal("ValidPrincipal");
        validTrade.setMtmValuationDate("2025-07-24");
        validTrade.setMtmValuation("1000");

        TradeDto invalidTrade = new TradeDto();
        invalidTrade.setPrincipal(null); // Invalid: Null principal
        invalidTrade.setMtmValuationDate("2025-07-24");
        invalidTrade.setMtmValuation("1000");

        List<TradeDto> records = Arrays.asList(validTrade, invalidTrade);

        // Act
        boolean result = strategy.shouldSkip(records);

        // Assert
        assertTrue(result, "The list should be skipped due to invalid record");
    }

    @Test
    void testFilterAndDeDuplicate() {
        // Arrange
        TradeDto trade1 = new TradeDto();
        trade1.setTradeRef("TR123");
        trade1.setPrincipal("Principal1");

        TradeDto trade2 = new TradeDto();
        trade2.setTradeRef("TR123");  // Duplicate tradeRef
        trade2.setPrincipal("Principal2");

        TradeDto trade3 = new TradeDto();
        trade3.setTradeRef("TR124");  // Unique tradeRef
        trade3.setPrincipal("Principal3");

        List<TradeDto> records = Arrays.asList(trade1, trade2, trade3);

        // Act
        Map<String, TradeDto> result = strategy.filterAndDeDuplicate(records, mock(FileMetadata.class), tradeService);

        // Assert
        assertEquals(2, result.size(), "There should be two unique records.");
        assertTrue(result.containsKey("TR123"), "Map should contain the TR123 record.");
        assertTrue(result.containsKey("TR124"), "Map should contain the TR124 record.");
    }

    @Test
    void testEnrichRecords() {
        // Arrange
        TradeDto trade = new TradeDto();
        trade.setTradeRef("TR123");
        trade.setPrincipal("ValidPrincipal");

        List<TradeDto> records = Arrays.asList(trade);
        when(clientRepo.findByShortName("ValidClient")).thenReturn(Optional.of(new Clients()));

        // Mock the service for finding the client
        when(principalRepo.findByPcmByName("ValidPrincipal"))
                .thenReturn(Optional.of(new Principals()));

        // Act
        List<TradeDto> enrichedRecords = strategy.enrichRecords(records, mock(FileMetadata.class), tradeService);

        // Assert
        assertEquals(1, enrichedRecords.size(), "There should be one enriched record.");
        assertEquals("TR123", enrichedRecords.get(0).getTradeRef(), "The trade reference should match.");
    }
}


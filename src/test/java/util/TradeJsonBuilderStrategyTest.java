package util;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.batch.json.TradeJsonBuilderStrategy;
import com.spring.batch.model.TradeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;

class TradeJsonBuilderStrategyTest {

    @InjectMocks
    private TradeJsonBuilderStrategy tradeJsonBuilderStrategy;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ObjectNode mockRoot;

    @Mock
    private ArrayNode mockCustomFieldArray;

    @Mock
    private TradeDto mockTradeDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mocking ObjectMapper's behavior
        when(objectMapper.createObjectNode()).thenReturn(mockRoot);
        when(objectMapper.createArrayNode()).thenReturn(mockCustomFieldArray);
    }

    @Test
    void testBuildJson_withValidTradeDto() {
        // Arrange
        when(mockTradeDto.getPrincipal()).thenReturn("principalValue");
        when(mockTradeDto.getCounterParty()).thenReturn("counterPartyValue");
        when(mockTradeDto.getTradeRef()).thenReturn("tradeRefValue");
        when(mockTradeDto.getMtmValuationDate()).thenReturn("2025-07-24");

        // Mock the methods that add custom fields
        when(mockTradeDto.getOrgCode()).thenReturn("orgCodeValue");

        // Act
        String json = tradeJsonBuilderStrategy.buildJson(mockTradeDto);

        // Assert
        assertNotNull(json, "JSON should not be null");
        assertTrue(json.contains("\"principal\":\"principalValue\""), "Principal field should be included in JSON");
        assertTrue(json.contains("\"counterParty\":\"counterPartyValue\""), "CounterParty field should be included in JSON");
        assertTrue(json.contains("\"tradeRef\":\"tradeRefValue\""), "TradeRef field should be included in JSON");
        assertTrue(json.contains("\"valuationDate\":\"2025-07-24\""), "ValuationDate should be included in JSON");
        assertTrue(json.contains("\"mtms\":["), "Mtms array should be in the JSON");
        assertTrue(json.contains("\"tradeDates\":["), "TradeDates array should be in the JSON");

        // Verify interaction with ObjectMapper
        verify(objectMapper, times(1)).createObjectNode();
        verify(objectMapper, times(1)).createArrayNode();
    }

    @Test
    void testBuildJson_withMissingFields() {
        // Arrange
        when(mockTradeDto.getPrincipal()).thenReturn(null);
        when(mockTradeDto.getCounterParty()).thenReturn(null);
        when(mockTradeDto.getTradeRef()).thenReturn(null);
        when(mockTradeDto.getMtmValuationDate()).thenReturn(null);

        // Act
        String json = tradeJsonBuilderStrategy.buildJson(mockTradeDto);

        // Assert
        assertNotNull(json, "JSON should not be null");
        assertFalse(json.contains("\"principal\":"), "Principal field should not be included when null");
        assertFalse(json.contains("\"counterParty\":"), "CounterParty field should not be included when null");
        assertFalse(json.contains("\"tradeRef\":"), "TradeRef field should not be included when null");
        assertFalse(json.contains("\"valuationDate\":"), "ValuationDate should not be included when null");

        // Verify that we added the correct fields to the root
        verify(objectMapper, times(1)).createObjectNode();
        verify(objectMapper, times(1)).createArrayNode();
    }

    @Test
    void testBuildJson_withCustomFields() {
        // Arrange
        when(mockTradeDto.getOrgCode()).thenReturn("orgCodeValue");

        // Act
        String json = tradeJsonBuilderStrategy.buildJson(mockTradeDto);

        // Assert
        assertTrue(json.contains("\"customFields\""), "Custom fields should be included in JSON");
        assertTrue(json.contains("\"numericField1\":\"orgCodeValue\""), "Custom field should be mapped correctly");

        // Verify interactions
        verify(objectMapper, times(1)).createObjectNode();
        verify(objectMapper, times(1)).createArrayNode();
    }

    @Test
    void testBuildJson_emptyTradeDto() {
        // Arrange
        when(mockTradeDto.getPrincipal()).thenReturn("");
        when(mockTradeDto.getCounterParty()).thenReturn("");
        when(mockTradeDto.getTradeRef()).thenReturn("");
        when(mockTradeDto.getMtmValuationDate()).thenReturn("");

        // Act
        String json = tradeJsonBuilderStrategy.buildJson(mockTradeDto);

        // Assert
        assertNotNull(json, "JSON should not be null");
        assertTrue(json.contains("\"principal\":\"\""), "Principal field should be included even if empty");
        assertTrue(json.contains("\"counterParty\":\"\""), "CounterParty field should be included even if empty");
        assertTrue(json.contains("\"tradeRef\":\"\""), "TradeRef field should be included even if empty");
        assertTrue(json.contains("\"valuationDate\":\"\""), "ValuationDate should be included even if empty");

        // Verify
        verify(objectMapper, times(1)).createObjectNode();
        verify(objectMapper, times(1)).createArrayNode();
    }
}

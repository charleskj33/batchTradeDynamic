package com.batch.test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.batch.json.JsonBuilderFactory;
import com.spring.batch.json.JsonBuilderStrategy;
import com.spring.batch.model.TradeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

class JsonBuilderFactoryTest {

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private JsonBuilderFactory jsonBuilderFactory;

    private JsonBuilderStrategy<TradeDto> tradeJsonBuilderStrategy;

    @BeforeEach
    void setUp() {
        // Initialize the mock tradeJsonBuilderStrategy
        tradeJsonBuilderStrategy = mock(JsonBuilderStrategy.class);

        // Reset mock behavior before each test
        reset(applicationContext, tradeJsonBuilderStrategy);
    }

    @Test
    void testGetBuilder_BeanExists() {
        // Arrange
        String tradeType = "trade";
        when(applicationContext.containsBean("tradejsonbuilderstrategy")).thenReturn(true);
        when(applicationContext.getBean("tradejsonbuilderstrategy")).thenReturn(tradeJsonBuilderStrategy);

        // Act
        JsonBuilderStrategy<TradeDto> builder = jsonBuilderFactory.getBuilder(tradeType);

        // Assert
        assertNotNull(builder, "Builder schould not be null");
        assertEquals(tradeJsonBuilderStrategy, builder, "The builder returned is incorrect");
        verify(applicationContext, times(1)).containsBean("tradejsonbuilderstrategy");
        verify(applicationContext, times(1)).getBean("tradejsonbuilderstrategy");
    }

    @Test
    void testGetBuilder_BeanDoesNotExist() {
        // Arrange
        String tradeType = "trade";
        when(applicationContext.containsBean("tradejsonbuilderstrategy")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            jsonBuilderFactory.getBuilder(tradeType);
        });
        assertEquals("No JsonBuilderStrategy found for tradeType: trade", exception.getMessage(),
                "Exception message is incorrect");

        verify(applicationContext, times(1)).containsBean("tradejsonbuilderstrategy");
        verify(applicationContext, times(0)).getBean("tradejsonbuilderstrategy");
    }

    @Test
    void testGetBuilder_BeanNameIsCaseInsensitive() {
        // Arrange
        String tradeType = "TRADE"; // testing uppercase trade type
        when(applicationContext.containsBean("tradejsonbuilderstrategy")).thenReturn(true);
        when(applicationContext.getBean("tradejsonbuilderstrategy")).thenReturn(tradeJsonBuilderStrategy);

        // Act
        JsonBuilderStrategy<TradeDto> builder = jsonBuilderFactory.getBuilder(tradeType);

        // Assert
        assertNotNull(builder);
        assertEquals(tradeJsonBuilderStrategy, builder);

        // Verifications
        verify(applicationContext, times(1)).containsBean("tradejsonbuilderstrategy");
        verify(applicationContext, times(1)).getBean("tradejsonbuilderstrategy");
    }
}

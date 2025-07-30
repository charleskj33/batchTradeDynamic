package com.batch.test.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.spring.batch.batch.processor.GenericTradeProcessor;
import com.spring.batch.batch.processor.TradeProcessorStrategy;
import com.spring.batch.model.*;
import com.spring.batch.service.NcsFeedDataService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class GenericTradeProcessorTest {

    @Mock
    private FileMetadata fileMetadata;

    @Mock
    private NcsFeedDataService tradeService;

    @Mock
    private TradeProcessorStrategy<BaseTradeDto> processorStrategy;

    private GenericTradeProcessor<BaseTradeDto> genericTradeProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        genericTradeProcessor = new GenericTradeProcessor<>(fileMetadata, tradeService, processorStrategy);
    }

    @Test
    void testProcess_success() throws Exception {
        BaseTradeDto tradeDto = mock(BaseTradeDto.class);
        when(tradeDto.getTradeRef()).thenReturn("Trade123");

        TradeDtoWrapper<BaseTradeDto> wrapper = mock(TradeDtoWrapper.class);

        when(processorStrategy.process(eq(tradeDto), eq(fileMetadata), eq(tradeService))).thenReturn(wrapper);

        TradeDtoWrapper<BaseTradeDto> result = genericTradeProcessor.process(tradeDto);

        assertNotNull(result);
        assertEquals(wrapper, result);
        verify(processorStrategy).process(tradeDto, fileMetadata, tradeService);
    }

    @Test
    void testProcess_exception() throws Exception {
        BaseTradeDto tradeDto = mock(BaseTradeDto.class);
        when(tradeDto.getTradeRef()).thenReturn("Trade123");

        when(processorStrategy.process(eq(tradeDto), eq(fileMetadata), eq(tradeService)))
                .thenThrow(new RuntimeException("Test Exception"));

        TradeDtoWrapper<BaseTradeDto> result = genericTradeProcessor.process(tradeDto);

        assertNull(result);

        verify(tradeService).addExcepAdd(argThat(entity ->
                entity.getTradeRef().equals("Trade123") && entity.getErrorMessage().contains("Processor exception")));
    }
}


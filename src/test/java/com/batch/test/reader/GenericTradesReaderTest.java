/*
package com.batch.test.reader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.spring.batch.batch.reader.GenericTradesReader;
import com.spring.batch.batch.reader.TradeReaderStrategy;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
import com.spring.batch.service.NcsFeedDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import java.util.*;

class GenericTradesReaderTest {

    @InjectMocks
    private GenericTradesReader<T> reader;

    @Mock
    private FlatFileItemReader<T> delegate;

    @Mock
    private TradeReaderStrategy<TradeDto> strategy;

    @Mock
    private NcsFeedDataService tradeService;

    @Mock
    private FileMetadata fileMetadata;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(delegate.read()).thenReturn(new TradeDto());  // Mock the reader behavior
    }

    @Test
    void testInitializeIterator() throws Exception {
        // Arrange
        List<TradeDto> records = Arrays.asList(new TradeDto());
        when(delegate.read()).thenReturn(new TradeDto(), null); // Mock reading 1 record
        when(strategy.shouldSkip(anyList())).thenReturn(false); // Ensure we don't skip records

        // Act
        GenericTradesReader<T> reader = new GenericTradesReader<>(delegate, strategy, fileMetadata, tradeService);
        Iterator<T> iterator = reader.initializeIterator(delegate, strategy);

        // Assert
        assertTrue(iterator.hasNext(), "Iterator should have next record.");
    }

    @Test
    void testRead() {
        // Arrange
        List<TradeDto> records = Arrays.asList(new TradeDto());
        when(delegate.read()).thenReturn(new TradeDto(), null);  // Mock reading 1 record

        // Act
        TradeDto record = reader.read();  // Read one record

        // Assert
        assertNotNull(record, "Record should not be null.");
    }

    @Test
    void testReadWhenNoMoreRecords() {
        // Arrange
        when(delegate.read()).thenReturn(null);  // No more records

        // Act
        TradeDto record = reader.read();

        // Assert
        assertNull(record, "Record should be null when there are no more records.");
    }
}

*/

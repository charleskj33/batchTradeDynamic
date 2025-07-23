package com.spring.batch.batch.reader;

import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.FileMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class GenericTradesReader<T extends BaseTradeDto> implements ItemReader<T> {

    private final Iterator<T> iterator;

    public GenericTradesReader(FlatFileItemReader<T> delegate,
                               TradeReaderStrategy<T> strategy,
                               FileMetadata fileMetadata) throws Exception {

        List<T> processedList;
        try {
            processedList = strategy.readAndPreprocess(delegate, fileMetadata);
        } catch (Exception e) {
            log.error("Error while preprocessing trade records", e);
            processedList = Collections.emptyList();
        }

        this.iterator = processedList.iterator();
    }

    @Override
    public T read() {
        return (iterator != null && iterator.hasNext()) ? iterator.next() : null;
    }
}

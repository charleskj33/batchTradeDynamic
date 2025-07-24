package com.spring.batch.batch.reader;

import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.service.NcsFeedDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;

import java.util.*;

@Slf4j
public class GenericTradesReader<T extends BaseTradeDto> implements ItemReader<T> {

    private final Iterator<T> iterator;
    private final FileMetadata fileMetadata;
    private final NcsFeedDataService ncsFeedDataService;

    public GenericTradesReader(FlatFileItemReader<T> delegate,
                               TradeReaderStrategy<T> strategy,
                               FileMetadata fileMetadata,
                               NcsFeedDataService ncsFeedDataService) throws Exception {
        this.ncsFeedDataService = ncsFeedDataService;
        this.fileMetadata = fileMetadata;

        this.iterator = initializeIterator(delegate, strategy);

    }

    private Iterator<T> initializeIterator(FlatFileItemReader<T> delegate, TradeReaderStrategy<T> strategy) {
        try{
            List<T> records = readRecords(delegate);

            if(strategy.shouldSkip(records)){
                strategy.handleValidationFailure(fileMetadata, ncsFeedDataService);
                return Collections.emptyIterator();
            }

            Map<String, T> uniRecords = strategy.filterAndDeDuplicate(records, fileMetadata,ncsFeedDataService);
            List<T> gdsTrades = strategy.enrichRecords(uniRecords.values(), fileMetadata, ncsFeedDataService);

            fileMetadata.setAllRecords(gdsTrades.stream()
                    .map(T::getTradeRef)
                    .toList());

            return new ArrayList<>(gdsTrades).iterator();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private List<T> readRecords(FlatFileItemReader<T> delegate) throws Exception {
        List<T> list = new ArrayList<>();
        delegate.open(new ExecutionContext());
        try {
            T item;
            while ((item = delegate.read()) != null) {
                list.add(item);
            }
        } catch (FlatFileParseException e) {
            log.error("Parse error in flat file: {}", e.getMessage());
        } finally {
            delegate.close();
        }
        return list;
    }

    @Override
    public T read() {
        return (iterator != null && iterator.hasNext()) ? iterator.next() : null;
    }
}

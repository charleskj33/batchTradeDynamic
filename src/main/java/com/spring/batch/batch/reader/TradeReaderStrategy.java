package com.spring.batch.batch.reader;

import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.FileMetadata;
import org.springframework.batch.item.file.FlatFileItemReader;

import java.util.List;

public interface TradeReaderStrategy<T extends BaseTradeDto> {

    List<T> readAndPreprocess(FlatFileItemReader<T> delegate, FileMetadata metadata) throws Exception;
}

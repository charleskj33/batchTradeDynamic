package com.spring.batch.batch.reader;

import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.service.NcsFeedDataService;
import jakarta.mail.MessagingException;

import java.util.*;

public interface TradeReaderStrategy<T extends BaseTradeDto> {

    //List<T> readAndPreprocess(FlatFileItemReader<T> delegate, FileMetadata metadata) throws Exception;

    boolean shouldSkip(List<T> records);

    void handleValidationFailure(FileMetadata fileMetadata, NcsFeedDataService ncsFeedDataService) throws MessagingException;

    Map<String, T> filterAndDeDuplicate(List<T> records, FileMetadata fileMetadata, NcsFeedDataService ncsFeedDataService);

    List<T> enrichRecords(Collection<T> records, FileMetadata fileMetadata, NcsFeedDataService ncsFeedDataService);
}

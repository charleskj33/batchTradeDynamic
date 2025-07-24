package com.spring.batch.batch.reader;

import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDto;
import com.spring.batch.repository.ClientRepo;
import com.spring.batch.repository.PrincipalRepo;
import com.spring.batch.service.NcsFeedDataService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("gdsTradeReaderStrategy")
@RequiredArgsConstructor
public class GdsTradeReaderStrategy implements TradeReaderStrategy<TradeDto> {

    private final NcsFeedDataService tradeService;
    private final PrincipalRepo principalRepo;
    private final ClientRepo clientRepo;

    @Override
    public boolean shouldSkip(List<TradeDto> records) {
        return false;
    }

    @Override
    public void handleValidationFailure(FileMetadata fileMetadata, NcsFeedDataService ncsFeedDataService) throws MessagingException {

    }

    @Override
    public Map<String, TradeDto> filterAndDeDuplicate(List<TradeDto> records, FileMetadata fileMetadata, NcsFeedDataService ncsFeedDataService) {
        return null;
    }

    @Override
    public List<TradeDto> enrichRecords(Collection<TradeDto> records, FileMetadata fileMetadata, NcsFeedDataService ncsFeedDataService) {
        return null;
    }

    /*@Override
    public List<TradeDto> readAndPreprocess(FlatFileItemReader<TradeDto> delegate,
                                            FileMetadata metadata) throws Exception {

        delegate.open(new ExecutionContext());
        List<TradeDto> trades = new ArrayList<>();

        try {
            TradeDto item;
            while ((item = delegate.read()) != null) {
                trades.add(item);
            }
        } finally {
            delegate.close();
        }

        // Sample logic: add validation, enrichment, deduplication here
        trades = enrichAndDeduplicate(trades, metadata);
        metadata.setAllRecords(trades.stream().map(TradeDto::getTradeRef).toList());

        return trades;
    }

    private List<TradeDto> enrichAndDeduplicate(List<TradeDto> rawTrades, FileMetadata metadata) {
        // Implement your GDS-specific deduplication logic here
        return rawTrades; // Replace with actual logic
    }*/
}


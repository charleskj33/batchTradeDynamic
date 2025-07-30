package com.spring.batch.batch.writer;

import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDtoWrapper;
import com.spring.batch.service.NcsFeedDataService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
//@Component("gdsTradeWriter")
@Component
@RequiredArgsConstructor
public class GenericTradeWriter<T extends BaseTradeDto> implements ItemWriter<TradeDtoWrapper<T>> {

    private final FileMetadata fileMetadata;
    private final NcsFeedDataService tradeService;
    private TradeWriterStrategy<T> writerStrategy;
    private final TradeWriterStrategyFactory writerStrategyFactory;

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void write(Chunk<? extends TradeDtoWrapper<T>> chunk) {
        String tradeType = fileMetadata.getSourceSystem();
        if (writerStrategy == null) {
            writerStrategy = writerStrategyFactory.getStrategy(tradeType);
        }
        try {
            writerStrategy.write(new ArrayList<>(chunk.getItems()), fileMetadata, tradeService);
        } catch (Exception e) {
            log.error("Error in writer strategy: {}", e.getMessage(), e);
            throw new RuntimeException("Writer failed", e);
        }
    }
}


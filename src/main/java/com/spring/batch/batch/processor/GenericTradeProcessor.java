package com.spring.batch.batch.processor;

import com.spring.batch.entity.ExceptionEntity;
import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDtoWrapper;
import com.spring.batch.service.NcsFeedDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*@Slf4j
@Component
@RequiredArgsConstructor
public class GenericTradeProcessor<T extends BaseTradeDto> implements ItemProcessor<T, TradeDtoWrapper<T>> {

    private final FileMetadata fileMetadata;
    private final NcsFeedDataService tradeService;
    private final TradeProcessorStrategyFactory strategyFactory;

    @Value("#{jobParameters['tradeType']}")
    private String tradeType;

    private TradeProcessorStrategy<T> processorStrategy;

    @Override
    public TradeDtoWrapper<T> process(T item) throws Exception {
        log.info("Processing tradeRef: {}", item.getTradeRef());
         String tradeType = fileMetadata.getSourceSystem();
        if (processorStrategy == null) {
            processorStrategy = strategyFactory.getStrategy(tradeType);
        }

        try {
            return processorStrategy.process(item, fileMetadata, tradeService);
        } catch (Exception e) {
            log.error("Unexpected error during processing: {}", e.getMessage(), e);
            String message = "Processor exception: " + e.getMessage();
            feedDataExcepEntity(fileMetadata.getBatchId(), tradeService, item.getTradeRef(), message);
            return null;
        }
    }

    private void feedDataExcepEntity(String batchId, NcsFeedDataService service, String tradeRef, String message) {
        ExceptionEntity entity = new ExceptionEntity();
        entity.setBatchId(batchId);
        entity.setTradeRef(tradeRef);
        entity.setErrorMessage(message);
        service.addExcepAdd(entity);
    }
}*/


@Slf4j
@Component("gdsTradeProcessor")
@RequiredArgsConstructor
public class GenericTradeProcessor<T extends BaseTradeDto> implements ItemProcessor<T, TradeDtoWrapper<T>> {

    private final FileMetadata fileMetadata;
    private final NcsFeedDataService tradeService;
    private final TradeProcessorStrategy<T> processorStrategy;

    @Override
    public TradeDtoWrapper<T> process(T item) throws Exception {
        log.info("Processing tradeRef: {}", item.getTradeRef());

        try {
            return processorStrategy.process(item, fileMetadata, tradeService);
        } catch (Exception e) {
            log.error("Unexpected error during processing: {}", e.getMessage(), e);
            String message = "Processor exception: " + e.getMessage();
            feedDataExcepEntity(fileMetadata.getBatchId(), tradeService, item.getTradeRef(), message);
            return null;
        }
    }

    private static void feedDataExcepEntity(String batchId, NcsFeedDataService ncsFeedDataService, String tradeRef, String errMsg){
        ExceptionEntity entity = new ExceptionEntity();
        entity.setBatchId(batchId);
        entity.setTradeRef(tradeRef);
        entity.setErrorMessage(errMsg);
        ncsFeedDataService.addExcepAdd(entity);
    }
}


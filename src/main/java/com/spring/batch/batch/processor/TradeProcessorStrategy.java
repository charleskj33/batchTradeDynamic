package com.spring.batch.batch.processor;

import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDtoWrapper;
import com.spring.batch.service.NcsFeedDataService;

public interface TradeProcessorStrategy<T extends BaseTradeDto> {
    TradeDtoWrapper<T> process(T trade, FileMetadata fileMetadata, NcsFeedDataService tradeService);
}


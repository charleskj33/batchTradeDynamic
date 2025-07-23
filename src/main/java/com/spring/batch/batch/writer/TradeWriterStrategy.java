package com.spring.batch.batch.writer;

import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.FileMetadata;
import com.spring.batch.model.TradeDtoWrapper;
import com.spring.batch.service.NcsFeedDataService;

import java.util.List;

public interface TradeWriterStrategy<T extends BaseTradeDto> {
    void write(List<TradeDtoWrapper<T>> items, FileMetadata metadata, NcsFeedDataService tradeService);
}


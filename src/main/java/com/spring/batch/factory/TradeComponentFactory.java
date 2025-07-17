package com.spring.batch.factory;

import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.TradeDtoWrapper;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

public interface TradeComponentFactory {
    <T extends BaseTradeDto> ItemReader<T> getReader(String tradeType);
    <T extends BaseTradeDto> ItemProcessor<T, TradeDtoWrapper<T>> getProcessor(String tradeType);
    <T extends BaseTradeDto> ItemWriter<TradeDtoWrapper<T>> getWriter(String tradeType);
}
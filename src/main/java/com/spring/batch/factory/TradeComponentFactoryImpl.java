package com.spring.batch.factory;

import com.spring.batch.model.BaseTradeDto;
import com.spring.batch.model.TradeDtoWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component  // <--- Required for Spring to manage this class
@RequiredArgsConstructor
public class TradeComponentFactoryImpl implements TradeComponentFactory {

    private final ApplicationContext context;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseTradeDto> ItemReader<T> getReader(String tradeType) {
        return (ItemReader<T>) context.getBean(tradeType.toLowerCase() + "TradeReader");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseTradeDto> ItemProcessor<T, TradeDtoWrapper<T>> getProcessor(String tradeType) {
        return (ItemProcessor<T, TradeDtoWrapper<T>>) context.getBean(tradeType.toLowerCase() + "TradeProcessor");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseTradeDto> ItemWriter<TradeDtoWrapper<T>> getWriter(String tradeType) {
        return (ItemWriter<TradeDtoWrapper<T>>) context.getBean(tradeType.toLowerCase() + "TradeWriter");
    }
}


/*
@Component
@RequiredArgsConstructor
public class TradeComponentFactoryImpl implements TradeComponentFactory {

    private final ApplicationContext context;

    @SuppressWarnings("unchecked")
    public <T extends BaseTradeDto> ItemReader<T> getReader(String tradeType) {
        return switch (tradeType.toUpperCase()) {
            case "GDS" -> (ItemReader<T>) context.getBean("gdsTradeReader");
            case "CKJ" -> (ItemReader<T>) context.getBean("ckjTradeReader");
            default -> throw new IllegalArgumentException("Invalid trade type: " + tradeType);
        };
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseTradeDto> ItemProcessor<T, TradeDtoWrapper<T>> getProcessor(String tradeType) {
        return (ItemProcessor<T, TradeDtoWrapper<T>>) context.getBean(tradeType.toLowerCase() + "TradeProcessor");
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseTradeDto> ItemWriter<TradeDtoWrapper<T>> getWriter(String tradeType) {
        return (ItemWriter<TradeDtoWrapper<T>>) context.getBean(tradeType.toLowerCase() + "TradeWriter");
    }
}*/

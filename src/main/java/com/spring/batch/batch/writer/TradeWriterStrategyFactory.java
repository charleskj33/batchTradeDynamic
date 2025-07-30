package com.spring.batch.batch.writer;

import com.spring.batch.model.BaseTradeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TradeWriterStrategyFactory {

    private final Map<String, TradeWriterStrategy<? extends BaseTradeDto>> strategyMap;

    @SuppressWarnings("unchecked")
    public <T extends BaseTradeDto> TradeWriterStrategy<T> getStrategy(String tradeType) {
        String beanName = tradeType.toLowerCase() + "TradeWriterStrategy";
        TradeWriterStrategy<T> strategy = (TradeWriterStrategy<T>) strategyMap.get(beanName);
        if (strategy == null) {
            throw new IllegalArgumentException("No writer strategy found for tradeType: " + tradeType);
        }
        return strategy;
    }
}

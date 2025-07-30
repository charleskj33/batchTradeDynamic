package com.spring.batch.batch.processor;


import com.spring.batch.model.BaseTradeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TradeProcessorStrategyFactory {

    private final Map<String, TradeProcessorStrategy<? extends BaseTradeDto>> strategyMap;

    @SuppressWarnings("unchecked")
    public <T extends BaseTradeDto> TradeProcessorStrategy<T> getStrategy(String tradeType) {
        String beanName = tradeType.toLowerCase() + "TradeProcessorStrategy";
        TradeProcessorStrategy<T> strategy = (TradeProcessorStrategy<T>) strategyMap.get(beanName);
        if (strategy == null) {
            throw new IllegalArgumentException("No processor strategy found for tradeType: " + tradeType);
        }
        return strategy;
    }
}


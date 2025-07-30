package com.spring.batch.batch.reader;

import com.spring.batch.model.BaseTradeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TradeReaderStrategyFactory {
    private final Map<String, TradeReaderStrategy<? extends BaseTradeDto>> strategyMap;

    @SuppressWarnings("unchecked")
    public <T extends BaseTradeDto> TradeReaderStrategy<T> getStrategy(String tradeType) {
        String beanName = tradeType.toLowerCase() + "TradeReaderStrategy";
        return (TradeReaderStrategy<T>) strategyMap.get(beanName);
    }
}


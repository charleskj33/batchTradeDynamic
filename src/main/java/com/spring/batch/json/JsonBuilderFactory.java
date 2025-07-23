package com.spring.batch.json;

import com.spring.batch.model.BaseTradeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonBuilderFactory {

    private final ApplicationContext context;

    @SuppressWarnings("unchecked")
    public <T extends BaseTradeDto> JsonBuilderStrategy<T> getBuilder(String tradeType) {
        String beanName = tradeType.toLowerCase() + "JsonBuilderStrategy";
        if (!context.containsBean(beanName)) {
            throw new IllegalArgumentException("No JsonBuilderStrategy found for tradeType: " + tradeType);
        }
        return (JsonBuilderStrategy<T>) context.getBean(beanName);
    }
}


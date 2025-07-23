package com.spring.batch.json;

import com.spring.batch.model.BaseTradeDto;

public interface JsonBuilderStrategy<T extends BaseTradeDto> {
    String buildJson(T trade);
}


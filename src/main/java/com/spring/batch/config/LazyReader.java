package com.spring.batch.config;

import com.spring.batch.model.BaseTradeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import com.spring.batch.factory.TradeComponentFactory;

@RequiredArgsConstructor
public class LazyReader<T extends BaseTradeDto> implements ItemReader<T> {

    private final TradeComponentFactory tradeComponentFactory;
    private final String tradeType;

    private ItemReader<T> delegate;

    @Override
    public T read() throws Exception {
        if (delegate == null) {
            delegate = tradeComponentFactory.getReader(tradeType); // Lazy init inside step
        }
        return delegate.read();
    }
}


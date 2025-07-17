package com.spring.batch.batch.processor;

import com.spring.batch.model.TradeDto;
import com.spring.batch.model.TradeDtoWrapper;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component("gdsTradeProcessor")
public class gdsTradeProcessor implements ItemProcessor<TradeDto, TradeDtoWrapper<TradeDto>> {
    @Override
    public TradeDtoWrapper<TradeDto> process(TradeDto item) throws Exception {
        return null;
    }
}

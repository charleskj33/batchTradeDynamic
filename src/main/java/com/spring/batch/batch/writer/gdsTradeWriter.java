package com.spring.batch.batch.writer;

import com.spring.batch.model.TradeDto;
import com.spring.batch.model.TradeDtoWrapper;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component("gdsTradeWriter")
public class gdsTradeWriter implements ItemWriter<TradeDtoWrapper<TradeDto>> {
    @Override
    public void write(Chunk<? extends TradeDtoWrapper<TradeDto>> chunk) throws Exception {

    }
}

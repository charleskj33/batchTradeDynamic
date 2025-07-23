package com.spring.batch.util;

public class TradeBeanNameUtil {
    public static String reader(TradeTypeEnum type) {
        return type.prefix() + "TradeReader";
    }

    public static String processor(TradeTypeEnum type) {
        return type.prefix() + "TradeProcessor";
    }

    public static String writer(TradeTypeEnum type) {
        return type.prefix() + "TradeWriter";
    }
}


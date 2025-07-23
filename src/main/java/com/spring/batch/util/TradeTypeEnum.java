package com.spring.batch.util;

public enum TradeTypeEnum {
    GDS("gds"),
    CKJ("ckj"),
    NCS("ncs");

    private final String prefix;

    TradeTypeEnum(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }
}


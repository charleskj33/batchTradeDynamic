package com.spring.batch.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "trade")
@Data
public class TradeConfigProperties {

    private Map<String, BlobConfig> configs;
    private Map<Integer, String> aidToType;

    @Data
    public static class BlobConfig {
        private String containerName;
        private String blobName;
    }

    public BlobConfig getByTradeType(String tradeType) {
        BlobConfig config = configs.get(tradeType.toLowerCase());
        if (config == null) {
            throw new IllegalArgumentException("Unknown tradeType: " + tradeType);
        }
        return config;
    }

    public String getTradeTypeByAid(int aid) {
        String type = aidToType.get(aid);
        if (type == null) {
            throw new IllegalArgumentException("Unknown AID: " + aid);
        }
        return type;
    }
}



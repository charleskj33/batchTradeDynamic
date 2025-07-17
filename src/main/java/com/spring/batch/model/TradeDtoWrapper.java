package com.spring.batch.model;

import lombok.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Component
@ToString
public class TradeDtoWrapper<T extends BaseTradeDto> {
    private T trade;
    private String clientName;
    private String externalId;
}
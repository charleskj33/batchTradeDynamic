package com.spring.batch.model;


import lombok.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Component
@ToString
public class TradeDto implements BaseTradeDto{

    private String principal;
    private String counterParty;
    private String product;
    private String orgCode;
    private String tradeRef;
    private String mtmValuation;
    private String mtmValuationDate;
    private String dealDate;
}
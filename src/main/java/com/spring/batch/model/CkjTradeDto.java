package com.spring.batch.model;

import lombok.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Component
@ToString
public class CkjTradeDto implements BaseTradeDto {
    private String tradeRef;
    private String mtmValuation;
    private String mtmValuationDate;
}
package com.spring.batch.dto;

import lombok.*;

@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
public class KafkaMessage {

    private int cId;
    private int aId;
    private String topic;
    private String status;
    private String time;
}

package com.spring.batch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "TRADE_FEED_MASTER")
public class TradeFeedMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileId;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private String batchId;

    @Column(nullable = false)
    private LocalDateTime insertedTimeStamp;

    @Column
    private String sourceSystem;

    @Column
    private int totalMsg;
}


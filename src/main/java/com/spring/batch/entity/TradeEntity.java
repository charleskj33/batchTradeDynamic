package com.spring.batch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Component
@ToString
@Entity
public class TradeEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String batchId;
        private String sourceSystem;
        private String feedType;
        private String jsonOutput;
        private String externalId;
        private LocalDateTime insertedTimeStamp;
        private String clientName;
        private String mode;
}

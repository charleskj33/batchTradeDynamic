package com.spring.batch.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Entity
@Table(name = "TRACKER")
@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TrackerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // or AUTO, depending on DB
    private Long id;
    private String batchId;
    private String service;
    private String status;
    private LocalDateTime insertedTimeStamp;
    private LocalDateTime updatedTimeStamp;
    private String desc;
}

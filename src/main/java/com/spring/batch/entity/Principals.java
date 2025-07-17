package com.spring.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Component
@ToString
@Entity
@Table(name="REF_PRINCIPALS")
public class Principals {

    @Id
    private String cmId;
    private String name;
    private String state;
    private String lookup;
    private String clientCmId;
}
package com.spring.batch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@ToString
@Table(name="REF_CPTYS")
public class Counterparty {
    @Id
    private String cmId;
    private String name;
    private String state;
    private String clientCmId;
    private String lookup;
}

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
@Entity
@ToString
@Table(name="REF_AGREEMENT")
public class Agreement {
    @Id
    private String cmId;
    private String name;
    private String principalCmId;
    private String cptyCmId;
    private String state;
    private String clientCmId;
    private String externalId;
    private String trTypes;
    private String masterAgType;
    private String agreeDate;

}

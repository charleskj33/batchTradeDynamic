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
@Component
@ToString
@Entity
@Table(name = "REF_CLIENTS")
public class Clients {

    @Id
    private String cmId;
    private String shortName;
    private String longName;
    private String state;
    private String country;
}

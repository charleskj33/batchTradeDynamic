package com.spring.batch.repository;


import com.spring.batch.entity.Clients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepo extends JpaRepository<Clients, String> {

    Optional<Clients> findByShortName(@Param("cCmId") String cCmId);
}

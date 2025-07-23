package com.spring.batch.repository;

import com.spring.batch.entity.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CounterPartyRepo extends JpaRepository<Counterparty, String> {

    @Query(value = "select cmId from Counterparty where lookup=:orgCode AND clientCmId=:cCmid AND state = 'A'", nativeQuery = true)
    Optional<String> findByCountName(@Param("orgCode") String orgCode, @Param("cCmid") String cCmid);
}

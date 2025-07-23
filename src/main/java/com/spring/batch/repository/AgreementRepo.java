package com.spring.batch.repository;

import com.spring.batch.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgreementRepo extends JpaRepository<Agreement, String> {

    @Query("SELECT a FROM Agreement a WHERE a.principalCmId = :pCmId AND a.cptyCmId = :cCmId AND a.state = 'A'")
    List<Agreement> findByExternalId(@Param("pCmId") String pCmId, @Param("cCmId") String cCmId);
}

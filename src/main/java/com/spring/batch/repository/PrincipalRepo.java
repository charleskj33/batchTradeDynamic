package com.spring.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.spring.batch.entity.Principals;

import java.util.Optional;

public interface PrincipalRepo extends JpaRepository<Principals, String> {

    /*@Query(value = "SELECT PcmId from PRINCIPALS where principalName=:pName")
    Optional<String> findByPcmId(@Param("pName") String pName);*/

    @Query(value = "SELECT * from Principals where lookup=:pName AND state = 'A'", nativeQuery = true)
    Optional<Principals> findByPcmByName(@Param("pName") String pName);
}

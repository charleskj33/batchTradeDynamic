package com.spring.batch.repository;

import com.spring.batch.model.TradeFeedMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeMasterRepo extends JpaRepository<TradeFeedMasterEntity, Long> {
}
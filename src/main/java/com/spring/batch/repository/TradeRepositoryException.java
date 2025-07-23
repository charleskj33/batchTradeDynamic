package com.spring.batch.repository;

import com.spring.batch.entity.ExceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepositoryException extends JpaRepository<ExceptionEntity, Long> {
}

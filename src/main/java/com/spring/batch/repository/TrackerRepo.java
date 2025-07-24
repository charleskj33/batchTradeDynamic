package com.spring.batch.repository;

import com.spring.batch.model.TrackerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackerRepo extends JpaRepository<TrackerEntity, Long> {
    Optional<TrackerEntity> findByBatchId(String batchId);
    Optional<TrackerEntity> findByBatchIdAndService(String batchId, String service);

}

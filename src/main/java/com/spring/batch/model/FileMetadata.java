package com.spring.batch.model;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Setter
@Getter
@Component
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class FileMetadata {

    private String batchId = UUID.randomUUID().toString();
    private AtomicInteger totalRecords = new AtomicInteger(0);
    private AtomicInteger processedRecords = new AtomicInteger(0);
    List<String> allRecords = new ArrayList<>();
    private Map<String, String> clientBatchIds = new HashMap<>();
    private Map<String, Integer> clientRecordCounts = new HashMap<>();
    private Map<String, String> clientModes = new HashMap<>();
    String sourceSystem;

    public String getClientModes(String clientName) {
        return clientModes.getOrDefault(clientName, "Flush");
    }

    public void setClientModes(String clientName, String mode) {
        clientModes.put(clientName, mode);
    }

    public String getBatchIdForClient(String clientName) {
        return clientBatchIds.computeIfAbsent(clientName, k -> UUID.randomUUID().toString());
    }

    public void incrementClientRecord(String clientName) {
        clientRecordCounts.merge(clientName, 1, Integer::sum);
        processedRecords.incrementAndGet();
    }

    public int getClientRecordCount(String clientName) {
        return clientRecordCounts.getOrDefault(clientName, 0);
    }

    public Set<String> getClients() {
        return clientBatchIds.keySet();
    }

    public int getTotalRecords() {
        return totalRecords.get();
    }

    public int getProcessedRecords() {
        return processedRecords.get();
    }

    public void incrementProcessRecords(int size){
        processedRecords.addAndGet(size);
    }

    public void incrementTotalRecords(int size){
        totalRecords.addAndGet(size);
    }

    public void reset(){
        this.batchId = UUID.randomUUID().toString();
        this.totalRecords.set(0);
        this.processedRecords.set(0);
        this.allRecords.clear();
        this.clientBatchIds.clear();
        this.clientRecordCounts.clear();
        this.clientModes.clear();
        this.sourceSystem = null;
    }
}

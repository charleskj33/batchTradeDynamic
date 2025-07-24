package com.spring.batch.listener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.batch.dto.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final GenericBatchService tradeBatchService;
    private final ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
               .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false);

    @KafkaListener(topics = "trade-info", groupId = "tradeGroup")
    public void consume(ConsumerRecord<String, String> consumerRecord) {
        try {

            String message = consumerRecord.value();
            KafkaMessage kafkaMessage = objectMapper.readValue(message, KafkaMessage.class);

            if(isValidMessage(kafkaMessage) && kafkaMessage.getStatus().equalsIgnoreCase("Success")) {
                tradeBatchService.triggerBatch(kafkaMessage);
            }else{
                log.info("its irrelvant message");
            }
        } catch (Exception e) {
            log.error("Error consuming Kafka message", e);
            throw new RuntimeException("Failed to consume Kafka message", e);
        }
    }

    private boolean isValidMessage(KafkaMessage kafkaMessage) {
        if (kafkaMessage == null) {
            log.warn("Received null kafka message");
            return false;
        }

        if (kafkaMessage.getStatus() == null || kafkaMessage.getAId() == 0) {
            log.warn("status/ or AId is empty");
            return false;
        }

        // Add other validation as needed
        return true;
    }
}


  /*public void consumeV1(String message) {
        try {
            ObjectMapper objectMapper = getObjectMapper();
            KafkaMessage kafkaMessage = objectMapper.readValue(message, KafkaMessage.class);
            tradeBatchService.triggerBatch(kafkaMessage);
        } catch (Exception e) {
            log.error("Error consuming Kafka message", e);
            throw new RuntimeException("Failed to consume Kafka message", e);
        }
    }*/


package com.spring.batch.Controller;

import com.spring.batch.listener.KafkaMessageListener;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trade")
public class TradeBatchController {

    private final KafkaMessageListener kafkaMessageListener;

    @PostMapping("/batch")
    public ResponseEntity<String> triggerBatch(@RequestBody String kafkaMsg) {
        try {
            kafkaMessageListener.consume(new ConsumerRecord<>("trade-info", 0, 0L, null, kafkaMsg));
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing batch: " + e.getMessage());
        }
    }
}


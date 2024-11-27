package com.chaw.concert.app.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, Object object) {
        kafkaTemplate.send(topic, object);
        log.info("Kafka Send Topic:{}, Message:{}", topic, object.toString());
    }
}

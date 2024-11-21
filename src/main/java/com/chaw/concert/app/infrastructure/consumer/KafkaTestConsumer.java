package com.chaw.concert.app.infrastructure.consumer;

import com.chaw.concert.app.infrastructure.kafka.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaTestConsumer {

    @KafkaListener(topics = KafkaTopics.TEST, groupId = KafkaTopics.GROUP_ID)
    public void consume(String message) {
        log.info("Received message: {}", message);
    }
}

package com.chaw.concert.app.infrastructure.consumer;

import com.chaw.concert.app.infrastructure.kafka.KafkaGroups;
import com.chaw.concert.app.infrastructure.kafka.TestKafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaTestConsumer {

    @KafkaListener(topics = TestKafkaTopics.TEST, groupId = KafkaGroups.GROUP_ID)
    public void consume(String message) {
        log.info("Received message: {}", message);
    }
}

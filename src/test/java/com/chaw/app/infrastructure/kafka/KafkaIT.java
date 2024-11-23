package com.chaw.app.infrastructure.kafka;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.infrastructure.consumer.KafkaTestConsumer;
import com.chaw.concert.app.infrastructure.kafka.KafkaProducer;
import com.chaw.concert.app.infrastructure.kafka.KafkaTopics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = ConcertApplication.class)
public class KafkaIT {

    @Autowired
    private KafkaProducer kafkaProducer;

    @SpyBean
    private KafkaTestConsumer kafkaTestConsumer;

    @Test
    public void 컨슈머_리스너가_정상적으로_실행() {
        kafkaProducer.sendMessage(KafkaTopics.TEST, "test-message");

        verify(kafkaTestConsumer, timeout(5000)).consume(anyString());
    }
}

package com.chaw.concert.app.infrastructure.consumer.concert;

import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReserveEvent;
import com.chaw.concert.app.infrastructure.kafka.KafkaProducer;
import com.chaw.concert.app.infrastructure.kafka.KafkaTopics;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@AllArgsConstructor
@Service
public class ReserveEventListener {

    private final KafkaProducer kafkaProducer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void saveOnDataPlatform(ReserveEvent reserveEvent) {
        kafkaProducer.sendMessage(KafkaTopics.CONCERT_RESERVE_TOPIC_DATASTORE, reserveEvent);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendToSlack(ReserveEvent reserveEvent) {
        kafkaProducer.sendMessage(KafkaTopics.CONCERT_RESERVE_TOPIC_SLACK, reserveEvent);
    }
}

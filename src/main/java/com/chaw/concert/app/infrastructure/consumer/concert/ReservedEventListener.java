package com.chaw.concert.app.infrastructure.consumer.concert;

import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReservedEvent;
import com.chaw.concert.app.infrastructure.kafka.KafkaProducer;
import com.chaw.concert.app.infrastructure.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Service
public class ReservedEventListener {

    private final KafkaProducer kafkaProducer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void saveOnDataPlatform(ReservedEvent reservedEvent) {
        kafkaProducer.sendMessage(KafkaTopics.CONCERT_RESERVE_TOPIC_DATASTORE, reservedEvent);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendToSlack(ReservedEvent reservedEvent) {
        kafkaProducer.sendMessage(KafkaTopics.CONCERT_RESERVE_TOPIC_SLACK, reservedEvent);
    }
}

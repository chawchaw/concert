package com.chaw.concert.app.infrastructure.consumer.concert;

import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PaidEvent;
import com.chaw.concert.app.infrastructure.kafka.KafkaProducer;
import com.chaw.concert.app.infrastructure.kafka.KafkaTopics;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@AllArgsConstructor
@Service
public class PaidEventListener {

    private final KafkaProducer kafkaProducer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void saveOnDataPlatform(PaidEvent paidEvent) {
        kafkaProducer.sendMessage(KafkaTopics.CONCERT_PAY_TOPIC_DATASTORE, paidEvent);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendMessageBySlack(PaidEvent paidEvent) {
        kafkaProducer.sendMessage(KafkaTopics.CONCERT_PAY_TOPIC_SLACK, paidEvent);
    }
}

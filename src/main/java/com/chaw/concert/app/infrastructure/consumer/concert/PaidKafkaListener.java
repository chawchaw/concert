package com.chaw.concert.app.infrastructure.consumer.concert;

import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxType;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertOutboxRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PaidEvent;
import com.chaw.concert.app.infrastructure.kafka.KafkaGroups;
import com.chaw.concert.app.infrastructure.kafka.PayKafkaTopics;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class PaidKafkaListener {

    private final ConcertOutboxRepository concertOutboxRepository;
    private final ConcertDataPlatformRepository concertDataPlatformRepository;
    private final SlackNotifierService slackNotifierService;

    private void logReceivedMessage(String topic) {
        log.info("Received from topic: {}", topic);
    }

    @KafkaListener(topics = PayKafkaTopics.CONCERT_PAY_TOPIC_DATASTORE, groupId = KafkaGroups.GROUP_ID)
    public void saveOnDataPlatform(PaidEvent paidEvent) {
        logReceivedMessage(PayKafkaTopics.CONCERT_PAY_TOPIC_DATASTORE);

        ConcertOutbox concertOutbox = concertOutboxRepository.findByIdAndTypeOrThrow(paidEvent.concertOutboxId(), ConcertOutboxType.PAID);
        concertOutbox.published();
        concertOutboxRepository.save(concertOutbox);

        concertDataPlatformRepository.savePay(paidEvent.concertScheduleId(), paidEvent.ticketId(), paidEvent.userId());
    }

    @KafkaListener(topics = PayKafkaTopics.CONCERT_PAY_TOPIC_SLACK, groupId = KafkaGroups.GROUP_ID)
    public void sendToSlack(PaidEvent paidEvent) {
        logReceivedMessage(PayKafkaTopics.CONCERT_PAY_TOPIC_SLACK);

        slackNotifierService.sendNotificationToSlack(paidEvent.toMessage());
    }
}

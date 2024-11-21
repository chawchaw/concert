package com.chaw.concert.app.infrastructure.consumer.concert;

import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PayEvent;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReserveEvent;
import com.chaw.concert.app.infrastructure.kafka.KafkaTopics;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class PayKafkaListener {

    private final ConcertDataPlatformRepository concertDataPlatformRepository;
    private final SlackNotifierService slackNotifierService;

    private void logReceivedMessage(String topic) {
        log.info("Received from topic: {}", topic);
    }

    @KafkaListener(topics = KafkaTopics.CONCERT_PAY_TOPIC_DATASTORE, groupId = KafkaTopics.GROUP_ID)
    public void saveOnDataPlatform(PayEvent payEvent) {
        logReceivedMessage(KafkaTopics.CONCERT_PAY_TOPIC_DATASTORE);

        concertDataPlatformRepository.savePay(payEvent.concertScheduleId(), payEvent.ticketId(), payEvent.userId());
    }

    @KafkaListener(topics = KafkaTopics.CONCERT_PAY_TOPIC_SLACK, groupId = KafkaTopics.GROUP_ID)
    public void sendToSlack(PayEvent payEvent) {
        logReceivedMessage(KafkaTopics.CONCERT_PAY_TOPIC_SLACK);

        slackNotifierService.sendNotificationToSlack(payEvent.toMessage());
    }
}

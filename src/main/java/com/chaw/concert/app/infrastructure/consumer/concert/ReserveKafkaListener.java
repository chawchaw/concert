package com.chaw.concert.app.infrastructure.consumer.concert;

import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
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
public class ReserveKafkaListener {

    private final ConcertDataPlatformRepository concertDataPlatformRepository;
    private final SlackNotifierService slackNotifierService;

    private void logReceivedMessage(String topic) {
        log.info("Received from topic: {}", topic);
    }

    @KafkaListener(topics = KafkaTopics.CONCERT_RESERVE_TOPIC_DATASTORE, groupId = KafkaTopics.GROUP_ID)
    public void saveOnDataPlatform(ReserveEvent reserveEvent) {
        logReceivedMessage(KafkaTopics.CONCERT_RESERVE_TOPIC_DATASTORE);

        concertDataPlatformRepository.saveReserve(reserveEvent.concertScheduleId(), reserveEvent.ticketId(), reserveEvent.userId());
    }

    @KafkaListener(topics = KafkaTopics.CONCERT_RESERVE_TOPIC_SLACK, groupId = KafkaTopics.GROUP_ID)
    public void sendToSlack(ReserveEvent reserveEvent) {
        logReceivedMessage(KafkaTopics.CONCERT_RESERVE_TOPIC_SLACK);

        slackNotifierService.sendNotificationToSlack(reserveEvent.toMessage());
    }
}

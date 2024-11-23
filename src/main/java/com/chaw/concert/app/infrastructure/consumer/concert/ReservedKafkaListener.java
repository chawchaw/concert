package com.chaw.concert.app.infrastructure.consumer.concert;

import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxType;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertOutboxRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReservedEvent;
import com.chaw.concert.app.infrastructure.kafka.KafkaTopics;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class ReservedKafkaListener {

    private final ConcertOutboxRepository concertOutboxRepository;
    private final ConcertDataPlatformRepository concertDataPlatformRepository;
    private final SlackNotifierService slackNotifierService;

    private void logReceivedMessage(String topic) {
        log.info("Received from topic: {}", topic);
    }

    @KafkaListener(topics = KafkaTopics.CONCERT_RESERVE_TOPIC_DATASTORE, groupId = KafkaTopics.GROUP_ID)
    public void saveOnDataPlatform(ReservedEvent reservedEvent) {
        logReceivedMessage(KafkaTopics.CONCERT_RESERVE_TOPIC_DATASTORE);

        ConcertOutbox concertOutbox = concertOutboxRepository.findByIdAndTypeOrThrow(reservedEvent.concertOutboxId(), ConcertOutboxType.RESERVED);
        concertOutbox.published();
        concertOutboxRepository.save(concertOutbox);

        concertDataPlatformRepository.saveReserve(reservedEvent.concertScheduleId(), reservedEvent.ticketId(), reservedEvent.userId());
    }

    @KafkaListener(topics = KafkaTopics.CONCERT_RESERVE_TOPIC_SLACK, groupId = KafkaTopics.GROUP_ID)
    public void sendToSlack(ReservedEvent reservedEvent) {
        logReceivedMessage(KafkaTopics.CONCERT_RESERVE_TOPIC_SLACK);

        slackNotifierService.sendNotificationToSlack(reservedEvent.toMessage());
    }
}

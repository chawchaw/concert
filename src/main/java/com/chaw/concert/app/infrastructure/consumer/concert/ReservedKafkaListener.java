package com.chaw.concert.app.infrastructure.consumer.concert;

import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxType;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertOutboxRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReservedEvent;
import com.chaw.concert.app.infrastructure.kafka.KafkaGroups;
import com.chaw.concert.app.infrastructure.kafka.ReserveKafkaTopics;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class ReservedKafkaListener {

    private final ConcertOutboxRepository concertOutboxRepository;
    private final ConcertDataPlatformRepository concertDataPlatformRepository;
    private final SlackNotifierService slackNotifierService;

    private void logReceivedMessage(String topic) {
        log.info("Received from topic: {}", topic);
    }

    @KafkaListener(topics = ReserveKafkaTopics.CONCERT_RESERVE_TOPIC_DATASTORE, groupId = KafkaGroups.GROUP_ID)
    public void saveOnDataPlatform(ReservedEvent reservedEvent) {
        logReceivedMessage(ReserveKafkaTopics.CONCERT_RESERVE_TOPIC_DATASTORE);

        ConcertOutbox concertOutbox = concertOutboxRepository.findByIdAndTypeOrThrow(reservedEvent.concertOutboxId(), ConcertOutboxType.RESERVED);
        concertOutbox.published();
        concertOutboxRepository.save(concertOutbox);

        concertDataPlatformRepository.saveReserve(reservedEvent.concertScheduleId(), reservedEvent.ticketId(), reservedEvent.userId());
    }

    @KafkaListener(topics = ReserveKafkaTopics.CONCERT_RESERVE_TOPIC_SLACK, groupId = KafkaGroups.GROUP_ID)
    public void sendToSlack(ReservedEvent reservedEvent) {
        logReceivedMessage(ReserveKafkaTopics.CONCERT_RESERVE_TOPIC_SLACK);

        slackNotifierService.sendNotificationToSlack(reservedEvent.toMessage());
    }
}

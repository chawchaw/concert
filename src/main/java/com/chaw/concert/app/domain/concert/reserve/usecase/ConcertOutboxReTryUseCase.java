package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertOutboxRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReservedEvent;
import com.chaw.concert.app.infrastructure.kafka.KafkaProducer;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class ConcertOutboxReTryUseCase {

    private final ConcertOutboxRepository concertOutboxRepository;
    private final KafkaProducer kafkaProducer;
    private final SlackNotifierService slackNotifierService;

    public void retry(ConcertOutbox concertOutbox) {
        concertOutbox.retried();
        concertOutboxRepository.save(concertOutbox);
        kafkaProducer.sendMessage(
                concertOutbox.getTopic(),
                ReservedEvent.builder()
                        .concertScheduleId(concertOutbox.getConcertScheduleId())
                        .ticketId(concertOutbox.getTicketId())
                        .userId(concertOutbox.getUserId())
                        .concertOutboxId(concertOutbox.getId())
                        .build());
    }

    public void fail(ConcertOutbox concertOutbox) {
        concertOutbox.failed();
        concertOutboxRepository.save(concertOutbox);
        slackNotifierService.sendNotificationToSlack(concertOutbox.toMessageForRetryFailed());
    }

    public void reTrySendToKafka() {
        List<ConcertOutboxStatus> statuses = ConcertOutbox.getRetryableStatuses();
        LocalDateTime before = ConcertOutbox.getRetryableBefore();

        List<ConcertOutbox> concertOutboxList = concertOutboxRepository.findAllByStatusInAndCreatedAtBefore(statuses, before);
        concertOutboxList
                .forEach(concertOutbox -> {
                    if (concertOutbox.isRetryable()) {
                        retry(concertOutbox);
                    } else {
                        fail(concertOutbox);
                    }
                });
    }
}

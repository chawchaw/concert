package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxStatus;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxType;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertOutboxRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.ConcertOutboxReTryUseCase;
import com.chaw.concert.app.infrastructure.kafka.KafkaProducer;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConcertOutboxReTryUseCaseUnitTest {

    @Mock
    private ConcertOutboxRepository concertOutboxRepository;
    @Mock
    private KafkaProducer kafkaProducer;
    @Mock
    private SlackNotifierService slackNotifierService;

    @InjectMocks
    private ConcertOutboxReTryUseCase concertOutboxReTryUseCase;

    @Test
    void test_retry() {
        // Given
        doNothing().when(concertOutboxRepository).save(any());
        doNothing().when(kafkaProducer).sendMessage(any(), any());

        // When
        ConcertOutbox concertOutbox = ConcertOutbox.builder()
                .id(1L)
                .status(ConcertOutboxStatus.INIT)
                .type(ConcertOutboxType.RESERVED)
                .concertScheduleId(1L)
                .ticketId(1L)
                .userId(1L)
                .retryCount(0)
                .build();
        concertOutboxReTryUseCase.retry(concertOutbox);

        // Then
        verify(concertOutboxRepository, times(1)).save(any());
        verify(kafkaProducer, times(1)).sendMessage(any(), any());
    }

    @Test
    void test_fail() {
        // Given
        doNothing().when(concertOutboxRepository).save(any());
        when(slackNotifierService.sendNotificationToSlack(any())).thenReturn(null);

        // When
        ConcertOutbox concertOutbox = ConcertOutbox.builder()
                .id(1L)
                .status(ConcertOutboxStatus.INIT)
                .type(ConcertOutboxType.RESERVED)
                .concertScheduleId(1L)
                .ticketId(1L)
                .userId(1L)
                .retryCount(0)
                .build();
        concertOutboxReTryUseCase.fail(concertOutbox);

        // Then
        verify(concertOutboxRepository, times(1)).save(any());
        verify(slackNotifierService, times(1)).sendNotificationToSlack(any());
    }

    @Test
    void test_reTrySendToKafka() {
        // Given
        when(concertOutboxRepository.findAllByStatusInAndCreatedAtBefore(any(), any())).thenReturn(List.of(
                ConcertOutbox.builder()
                        .id(1L)
                        .status(ConcertOutboxStatus.INIT)
                        .type(ConcertOutboxType.RESERVED)
                        .retryCount(0)
                        .build()
        ));

        // When
        concertOutboxReTryUseCase.reTrySendToKafka();

        // Then
        verify(concertOutboxRepository, times(1)).findAllByStatusInAndCreatedAtBefore(any(), any());
    }
}

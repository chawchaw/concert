package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxStatus;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxType;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertOutboxRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.ConcertOutboxReTryUseCase;
import com.chaw.concert.app.infrastructure.kafka.KafkaProducer;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class ConcertOutboxReTryUseCaseIT {

    @Autowired
    private ConcertOutboxRepository concertOutboxRepository;

    @Autowired
    private ConcertOutboxReTryUseCase concertOutboxReTryUseCase;

    @SpyBean
    private KafkaProducer kafkaProducer;

    @SpyBean
    private SlackNotifierService slackNotifierService;

    @Test
    void 발행_3번_시도후에_발행이_안되면_실패처리() {
        // Given
        List<ConcertOutbox> concertOutboxList = List.of(
                ConcertOutbox.builder()
                        .id(1L)
                        .status(ConcertOutboxStatus.INIT)
                        .type(ConcertOutboxType.RESERVED)
                        .retryCount(0)
                        .createdAt(LocalDateTime.now().minusMinutes(6))
                        .build(),
                ConcertOutbox.builder()
                        .id(2L)
                        .status(ConcertOutboxStatus.RETRY)
                        .type(ConcertOutboxType.RESERVED)
                        .retryCount(1)
                        .createdAt(LocalDateTime.now().minusMinutes(6))
                        .build(),
                ConcertOutbox.builder()
                        .id(3L)
                        .status(ConcertOutboxStatus.RETRY)
                        .type(ConcertOutboxType.RESERVED)
                        .retryCount(3)
                        .createdAt(LocalDateTime.now().minusMinutes(6))
                        .build()
        );
        concertOutboxList.forEach(concertOutboxRepository::save);

        // When
        concertOutboxReTryUseCase.reTrySendToKafka();

        // Then
        List<ConcertOutbox> concertOutboxListResult = concertOutboxRepository.findAll();
        assertEquals(3, concertOutboxListResult.size());

        long countRetry = concertOutboxListResult.stream()
                .filter(concertOutbox -> concertOutbox.getStatus().equals(ConcertOutboxStatus.RETRY)).count();
        long countFailed = concertOutboxListResult.stream()
                .filter(concertOutbox -> concertOutbox.getStatus().equals(ConcertOutboxStatus.FAILED)).count();
        assertEquals(2, countRetry);
        assertEquals(1, countFailed);

        verify(kafkaProducer, times(2)).sendMessage(any(), any());
        verify(slackNotifierService, times(1)).sendNotificationToSlack(any());
    }
}

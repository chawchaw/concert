package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.infrastructure.consumer.concert.ReserveEventListener;
import com.chaw.concert.app.infrastructure.consumer.concert.ReserveKafkaListener;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveEventRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.ReserveUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReserveEvent;
import com.chaw.concert.app.infrastructure.kafka.KafkaProducer;
import com.chaw.concert.app.infrastructure.kafka.KafkaTopics;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class ReserveUseCaseIT {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @SpyBean
    private ReserveEventRepository reserveEventRepository;

    @SpyBean
    private ReserveEventListener reserveEventListener;

    @SpyBean
    private KafkaProducer kafkaProducer;

    @SpyBean
    private ReserveKafkaListener reserveKafkaListener;

    @SpyBean
    private ConcertDataPlatformRepository concertDataPlatformRepository;

    @SpyBean
    private SlackNotifierService slackNotifierService;

    @Autowired
    private ReserveUseCase reserveUseCase;

    private Concert concert1;
    private ConcertSchedule concertSchedule1;
    private Ticket ticket1;

    @BeforeEach
    void setUp() {
        concert1 = Concert.builder()
                .name("concert1")
                .build();
        concertRepository.save(concert1);

        concertSchedule1 = ConcertSchedule.builder()
                .concertId(concert1.getId())
                .isSoldOut(false)
                .totalSeat(10)
                .availableSeat(10)
                .dateConcert(LocalDateTime.now().plusDays(1))
                .build();
        concertScheduleRepository.save(concertSchedule1);

        ticket1 = Ticket.builder()
                .concertScheduleId(concertSchedule1.getId())
                .build();
        ticketRepository.save(ticket1);
    }

    @Test
    void 카프카_발행과_컨슘이_잘_동작했는지_확인() {
        // Given
        Long userId = 1L;
        ReserveEvent reserveEvent = new ReserveEvent(concertSchedule1.getId(), ticket1.getId(), userId);
        ReserveUseCase.Input input = new ReserveUseCase.Input(userId, ticket1.getId());

        // When
        Long startTime = System.currentTimeMillis();
        ReserveUseCase.Output output = reserveUseCase.execute(input);
        Long endTime = System.currentTimeMillis();
        Long elapsedTime = endTime - startTime;
        System.out.println("소요시간: " + elapsedTime + "ms");

        // Then
        assertEquals(true, output.success());

        verify(reserveEventRepository, timeout(1000)).complete(reserveEvent);
        verify(reserveEventListener, timeout(1000)).saveOnDataPlatform(reserveEvent);
        verify(kafkaProducer, timeout(1000)).sendMessage(KafkaTopics.CONCERT_RESERVE_TOPIC_DATASTORE, reserveEvent);
        verify(kafkaProducer, timeout(1000)).sendMessage(KafkaTopics.CONCERT_RESERVE_TOPIC_SLACK, reserveEvent);
        verify(reserveKafkaListener, timeout(5000)).saveOnDataPlatform(reserveEvent);
        verify(reserveKafkaListener, timeout(5000)).sendToSlack(reserveEvent);
        verify(concertDataPlatformRepository, timeout(5000)).saveReserve(reserveEvent.concertScheduleId(), reserveEvent.ticketId(), reserveEvent.userId());
        verify(slackNotifierService, timeout(5000)).sendNotificationToSlack(reserveEvent.toMessage());
    }

}

package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxStatus;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxType;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertOutboxRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.PaidEventRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PaidEvent;
import com.chaw.concert.app.infrastructure.consumer.concert.PaidEventListener;
import com.chaw.concert.app.infrastructure.consumer.concert.PaidKafkaListener;
import com.chaw.concert.app.infrastructure.kafka.KafkaProducer;
import com.chaw.concert.app.infrastructure.kafka.PayKafkaTopics;
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
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class PayUseCaseIT {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ConcertOutboxRepository concertOutboxRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @SpyBean
    private PaidEventRepository paidEventRepository;

    @SpyBean
    private PaidEventListener paidEventListener;

    @SpyBean
    private KafkaProducer kafkaProducer;

    @SpyBean
    private PaidKafkaListener paidKafkaListener;

    @SpyBean
    private ConcertDataPlatformRepository concertDataPlatformRepository;

    @SpyBean
    private SlackNotifierService slackNotifierService;

    @Autowired
    private PayUseCase payUseCase;

    private Long userId = 1L;
    private Integer balance = 1000;
    private Integer price = 100;

    private Point point;
    private Concert concert;
    private ConcertSchedule concertSchedule;
    private Ticket ticket;
    private Reserve reserve;

    @BeforeEach
    void setUp() {
        point = Point.builder()
                .userId(userId)
                .balance(balance)
                .build();
        pointRepository.save(point);

        concert = Concert.builder()
                .name("concert")
                .build();
        concertRepository.save(concert);

        concertSchedule = ConcertSchedule.builder()
                .concertId(concert.getId())
                .isSoldOut(false)
                .totalSeat(10)
                .availableSeat(10)
                .dateConcert(LocalDateTime.now().plusDays(1))
                .build();
        concertScheduleRepository.save(concertSchedule);

        ticket = Ticket.builder()
                .concertScheduleId(concertSchedule.getId())
                .price(price)
                .build();
        ticketRepository.save(ticket);

        reserve = new Reserve(ticket.getConcertScheduleId(), ticket.getId(), userId);
        reserveRepository.save(reserve);
    }

    @Test
    void 결제_성공시_이벤트리스너가_데이터_플랫폼에_결제정보_전달() {
        // when
        Long startTime = System.currentTimeMillis();
        PayUseCase.Input input = new PayUseCase.Input(userId, ticket.getId());
        PayUseCase.Output output = payUseCase.execute(input);
        Long endTime = System.currentTimeMillis();
        Long elapsedTime = endTime - startTime;
        System.out.println("소요시간: " + elapsedTime + "ms");

        // then
        assertEquals(true, output.success());
        ConcertOutbox concertOutbox = concertOutboxRepository.findByIdAndTypeOrThrow(output.concertOutboxId(), ConcertOutboxType.PAID);
        assertEquals(ConcertOutboxStatus.INIT, concertOutbox.getStatus());

        PaidEvent paidEvent = PaidEvent.builder()
                .concertScheduleId(concertSchedule.getId())
                .ticketId(ticket.getId())
                .userId(userId)
                .concertOutboxId(output.concertOutboxId())
                .build();
        verify(paidEventRepository, timeout(1000)).complete(paidEvent);
        verify(paidEventListener, timeout(1000)).saveOnDataPlatform(paidEvent);
        verify(kafkaProducer, timeout(1000)).sendMessage(PayKafkaTopics.CONCERT_PAY_TOPIC_DATASTORE, paidEvent);
        verify(kafkaProducer, timeout(1000)).sendMessage(PayKafkaTopics.CONCERT_PAY_TOPIC_SLACK, paidEvent);
        verify(paidKafkaListener, timeout(5000)).saveOnDataPlatform(paidEvent);
        verify(paidKafkaListener, timeout(5000)).sendToSlack(paidEvent);
        verify(concertDataPlatformRepository, timeout(5000)).savePay(paidEvent.concertScheduleId(), paidEvent.ticketId(), paidEvent.userId());
        verify(slackNotifierService, timeout(5000)).sendNotificationToSlack(paidEvent.toMessage());

        ConcertOutbox concertOutboxAfterConsume = concertOutboxRepository.findByIdAndTypeOrThrow(output.concertOutboxId(), ConcertOutboxType.PAID);
        assertEquals(ConcertOutboxStatus.PUBLISHED, concertOutboxAfterConsume.getStatus());
    }

}

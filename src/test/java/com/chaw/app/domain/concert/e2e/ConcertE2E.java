package com.chaw.app.domain.concert.e2e;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.common.user.usecase.ChargePoint;
import com.chaw.concert.app.domain.concert.query.entity.*;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesNotSoldOut;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcerts;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsInEmptyStatus;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.scheduler.PassWaitQueue;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitQueue;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicket;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserve;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.ChargePointInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
public class ConcertE2E {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    @Autowired
    private WaitQueueRepository waitQueueRepository;
    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private ReserveRepository reserveRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PassWaitQueue passWaitQueue;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String host = "http://localhost:8080/api/v1";
    private final String uuid = "123e4567-e89b-12d3-a456-426614174000";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        pointRepository.deleteAll();
        pointHistoryRepository.deleteAll();
        waitQueueRepository.deleteAll();
        concertRepository.deleteAll();
        concertScheduleRepository.deleteAll();
        ticketRepository.deleteAll();
        reserveRepository.deleteAll();
        paymentRepository.deleteAll();

        User user = User.builder()
                .name("User1")
                .uuid("123e4567-e89b-12d3-a456-426614174000")
                .build();
        userRepository.save(user);

        Concert concert = Concert.builder()
                .name("Concert1")
                .info("Concert1 Info")
                .artist("Artist1")
                .host("Host1")
                .build();
        concertRepository.save(concert);

        ConcertSchedule concertSchedule = ConcertSchedule.builder()
                .concertId(concert.getId())
                .isSoldOut(false)
                .totalSeat(50)
                .availableSeat(50)
                .dateConcert(LocalDateTime.now().plusDays(1))
                .build();
        concertScheduleRepository.save(concertSchedule);

        Ticket ticket = Ticket.builder()
                .concertScheduleId(concertSchedule.getId())
                .type(TicketType.VIP)
                .status(TicketStatus.EMPTY)
                .price(100000)
                .seatNo("A1")
                .build();
        ticketRepository.save(ticket);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        pointRepository.deleteAll();
        pointHistoryRepository.deleteAll();
        waitQueueRepository.deleteAll();
        concertRepository.deleteAll();
        concertScheduleRepository.deleteAll();
        ticketRepository.deleteAll();
        reserveRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    /**
     * 대기열 입장 -> 통과 -> 콘서트조회 -> 일정조회 -> 티켓조회 -> 예약 -> 결제(잔액실패) -> 포인트 충전 -> 결제(성공)
     */
    @Test
    @Disabled // 로컬 서버 실행 필요
    void success_pay() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("uuid", uuid);

        // 대기열 입장
        ResponseEntity<EnterWaitQueue.Output> enterWaitQueueResult = restTemplate.exchange(
                MessageFormat.format("{0}/concert/queue", host),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                EnterWaitQueue.Output.class
        );
        assertEquals(WaitQueueStatus.WAIT.name(), enterWaitQueueResult.getBody().status());

        // 통과
        passWaitQueue.execute();
        ResponseEntity<EnterWaitQueue.Output> enterWaitQueueResultAfterPass = restTemplate.exchange(
                MessageFormat.format("{0}/concert/queue", host),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                EnterWaitQueue.Output.class
        );
        assertEquals(WaitQueueStatus.PASS.name(), enterWaitQueueResultAfterPass.getBody().status());

        // 콘서트조회
        ResponseEntity<GetConcerts.Output> getConcertsResult = restTemplate.exchange(
                MessageFormat.format("{0}/concert", host),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GetConcerts.Output.class
        );
        assertEquals(1, getConcertsResult.getBody().concerts().size());
        GetConcerts.Output.ConcertOutput concert = getConcertsResult.getBody().concerts().get(0);

        // 일정조회
        ResponseEntity<GetConcertSchedulesNotSoldOut.Output> getSchedulesResult = restTemplate.exchange(
                MessageFormat.format("{0}/concert/{1}/schedule", host, concert.id()),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GetConcertSchedulesNotSoldOut.Output.class
        );
        assertEquals(1, getSchedulesResult.getBody().schedules().size());
        GetConcertSchedulesNotSoldOut.Output.Item concertSchedule = getSchedulesResult.getBody().schedules().get(0);

        // 티켓조회
        ResponseEntity<GetTicketsInEmptyStatus.Output> getTicketsResult = restTemplate.exchange(
                MessageFormat.format("{0}/concert/{1}/schedule/{2}/tickets", host, concert.id(), concertSchedule.id()),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GetTicketsInEmptyStatus.Output.class
        );
        assertEquals(1, getTicketsResult.getBody().tickets().size());
        GetTicketsInEmptyStatus.Output.Item ticket = getTicketsResult.getBody().tickets().get(0);

        // 예약
        ResponseEntity<RequestReserve.Output> reserveResult = restTemplate.exchange(
                MessageFormat.format("{0}/concert/{1}/schedule/{2}/tickets/{3}/reserve", host, concert.id(), concertSchedule.id(), ticket.id()),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                RequestReserve.Output.class
        );
        assertEquals(true, reserveResult.getBody().success());

        // 결제(잔액실패)
        try {
            restTemplate.exchange(
                    MessageFormat.format("{0}/concert/{1}/schedule/{2}/tickets/{3}/pay", host, concert.id(), concertSchedule.id(), ticket.id()),
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    PayTicket.Output.class
            );
        } catch (HttpClientErrorException e) {
            assertEquals(404, e.getRawStatusCode());
        }

        // 포인트 충전
        ChargePointInput chargePointInput = new ChargePointInput(100000);
        ResponseEntity<ChargePoint.Output> chargePointResult = restTemplate.exchange(
                MessageFormat.format("{0}/user/point/charge", host),
                HttpMethod.POST,
                new HttpEntity<>(chargePointInput, headers),
                ChargePoint.Output.class
        );
        assertEquals(chargePointInput.point(), chargePointResult.getBody().balance());

        // 결제(성공)
        ResponseEntity<PayTicket.Output> payResult = restTemplate.exchange(
                MessageFormat.format("{0}/concert/{1}/schedule/{2}/tickets/{3}/pay", host, concert.id(), concertSchedule.id(), ticket.id()),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                PayTicket.Output.class
        );
        assertEquals(true, payResult.getBody().success());
    }

    @Test
    @Disabled // 테스트 데이터 만들기
    void testForData() {
        // do nothing
    }
}

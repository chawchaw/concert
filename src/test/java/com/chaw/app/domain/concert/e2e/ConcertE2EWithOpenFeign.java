package com.chaw.app.domain.concert.e2e;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.domain.concert.query.entity.*;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.scheduler.PassWaitQueue;
import com.chaw.concert.app.infrastructure.feign.client.AuthFeignClient;
import com.chaw.concert.app.infrastructure.feign.client.ConcertFeignClient;
import com.chaw.concert.app.infrastructure.feign.client.QueueFeignClient;
import com.chaw.concert.app.infrastructure.feign.client.UserFeignClient;
import com.chaw.concert.app.presenter.controller.api.v1.concert.dto.*;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.ChargePointInput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.ChargePointOutput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.LoginInput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.LoginOutput;
import com.chaw.helper.DatabaseCleanupListener;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class ConcertE2EWithOpenFeign {

    @Autowired
    private AuthFeignClient authFeignClient;

    @Autowired
    private QueueFeignClient queueFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ConcertFeignClient concertFeignClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PassWaitQueue passWaitQueue;

    private final String username = "user1";
    private final String password = "password";

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
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

    /**
     * 로그인
     * -> 대기열 입장 -> 대기열 스케줄러 동작 -> 통과
     * -> 콘서트조회 -> 일정조회 -> 티켓조회
     * -> 예약 -> 결제(잔액실패) -> 포인트 충전 -> 결제(성공)
     */
    @Test
    @Disabled
    // 로컬 서버 실행 필요
    void success_pay() {
        // 로그인
        LoginOutput loginResponse = authFeignClient.login(new LoginInput(username, password));
        String token = loginResponse.token();
        assertNotNull(token);

        // Authorization 헤더를 위한 Bearer 토큰
        String authHeader = "Bearer " + token;

        // 대기열 입장
        EnterWaitQueueOutput queueResponse = queueFeignClient.enter(authHeader);
        assertEquals(WaitQueueStatus.WAIT.name(), queueResponse.status());

        // 스케줄러 동작
        passWaitQueue.execute();

        // 대기열 통과
        queueResponse = queueFeignClient.enter(authHeader);
        assertEquals(WaitQueueStatus.PASS.name(), queueResponse.status());

        // 콘서트 조회
        GetConcertsOutput concertsResponse = concertFeignClient.getConcerts(authHeader);
        assertEquals(1, concertsResponse.concerts().size());
        Long concertId = concertsResponse.concerts().get(0).id();

        // 일정 조회
        GetConcertSchedulesNotSoldOutOutput schedulesResponse = concertFeignClient.getSchedules(authHeader, concertId);
        assertEquals(1, schedulesResponse.schedules().size());
        Long scheduleId = schedulesResponse.schedules().get(0).id();

        // 티켓 조회
        GetTicketsInEmptyStatusOutput ticketsResponse = concertFeignClient.getTickets(authHeader, concertId, scheduleId);
        assertEquals(1, ticketsResponse.tickets().size());
        Long ticketId = ticketsResponse.tickets().get(0).id();

        // 예약
        RequestReserveOutput reserveResponse = concertFeignClient.reserveTicket(authHeader, concertId, scheduleId, ticketId);
        assertTrue(reserveResponse.success());

        // 결제 실패 (잔액 부족)
        FeignException feignException = assertThrows(FeignException.class, () -> {
            concertFeignClient.payTicket(authHeader, concertId, scheduleId, ticketId);
        });
        assertEquals(409, feignException.status());

        // 포인트 충전 (따로 포인트 충전 FeignClient 생성 필요)
        Integer point = 100000;
        ChargePointOutput chargePointResponse = userFeignClient.chargePoint(authHeader, new ChargePointInput(point));
        assertEquals(point, chargePointResponse.balance());; // 포인트 잔액 확인

        // 결제 성공
        PayTicketOutput payResponse = concertFeignClient.payTicket(authHeader, concertId, scheduleId, ticketId);
        assertTrue(payResponse.success()); // 결제 성공 확인
    }
}

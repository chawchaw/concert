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
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.ChargePointInput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.LoginInput;
import com.chaw.helper.DatabaseCleanupListener;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class ConcertE2EWithRestAssured {

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

    private final String host = "http://localhost:8080/api/v1";
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
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(new LoginInput(username, password))
                .post(host + "/auth/login")
                .then()
                .statusCode(200)
                .extract().response();
        String token = loginResponse.jsonPath().getString("token");
        assertNotNull(token);

        RequestSpecification requestSpec = given()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json");

        // 대기열 입장
        requestSpec
                .post(host + "/queue/enter")
                .then()
                .statusCode(200)
                .body("status", equalTo(WaitQueueStatus.WAIT.name()))
                .extract().response();

        // 스케줄러 동작
        passWaitQueue.execute();

        // 대기열 통과
        requestSpec
                .post(host + "/queue/enter")
                .then()
                .statusCode(200)
                .body("status", equalTo(WaitQueueStatus.PASS.name()));

        // 콘서트 조회
        Response getConcertsResponse = requestSpec
                .get(host + "/concert")
                .then()
                .statusCode(200)
                .body("concerts.size()", equalTo(1))
                .extract().response();
        Long concertId = getConcertsResponse.jsonPath().getLong("concerts[0].id");

        // 일정 조회
        Response getSchedulesResponse = requestSpec
                .get(host + "/concert/" + concertId + "/schedule")
                .then()
                .statusCode(200)
                .body("schedules.size()", equalTo(1))
                .extract().response();
        Long scheduleId = getSchedulesResponse.jsonPath().getLong("schedules[0].id");

        // 티켓 조회
        Response getTicketsResponse = requestSpec
                .get(host + "/concert/" + concertId + "/schedule/" + scheduleId + "/tickets")
                .then()
                .statusCode(200)
                .body("tickets.size()", equalTo(1))
                .extract().response();
        Long ticketId = getTicketsResponse.jsonPath().getLong("tickets[0].id");

        // 예약
        requestSpec
                .post(host + "/concert/" + concertId + "/schedule/" + scheduleId + "/tickets/" + ticketId + "/reserve")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));

        // 결제 실패(잔액 부족)
        requestSpec
                .post(host + "/concert/" + concertId + "/schedule/" + scheduleId + "/tickets/" + ticketId + "/pay")
                .then()
                .statusCode(409);

        // 포인트 충전
        requestSpec
                .body(new ChargePointInput(100000))
                .post(host + "/user/point/charge")
                .then()
                .statusCode(200)
                .body("balance", equalTo(100000));

        // 결제 성공
        requestSpec
                .post(host + "/concert/" + concertId + "/schedule/" + scheduleId + "/tickets/" + ticketId + "/pay")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }
}

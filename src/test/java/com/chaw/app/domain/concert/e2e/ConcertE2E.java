package com.chaw.app.domain.concert.e2e;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.User;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.common.user.repository.UserRepository;
import com.chaw.concert.app.domain.concert.query.entity.*;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

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
                .isSold(false)
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

    @Test
    void makeTestData() {
        // do nothing
    }
}

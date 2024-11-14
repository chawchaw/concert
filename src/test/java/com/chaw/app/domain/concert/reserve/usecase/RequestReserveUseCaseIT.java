package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserveRedissonRLockUseCase;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class RequestReserveUseCaseIT {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @MockBean
    private ConcertDataPlatformRepository concertDataPlatformRepository;

    @Autowired
    private RequestReserveRedissonRLockUseCase requestReserveRedissonRLockUseCase;

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
    void 예약_성공시_이벤트리스너가_데이터_플랫폼에_예약정보_전달() {
        Long userId = 1L;
        // Given
        RequestReserveRedissonRLockUseCase.Input input = new RequestReserveRedissonRLockUseCase.Input(userId, ticket1.getId());

        // When
        Long startTime = System.currentTimeMillis();
        RequestReserveRedissonRLockUseCase.Output output = requestReserveRedissonRLockUseCase.execute(input);
        Long endTime = System.currentTimeMillis();
        Long elapsedTime = endTime - startTime;
        System.out.println("소요시간: " + elapsedTime + "ms");

        // Then
        assertEquals(true, output.success());
        verify(concertDataPlatformRepository, times(1)).saveReserve(concertSchedule1.getId(), ticket1.getId(), userId);
    }

}

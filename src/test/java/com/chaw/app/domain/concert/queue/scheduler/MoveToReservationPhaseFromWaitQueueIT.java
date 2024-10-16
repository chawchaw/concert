package com.chaw.app.domain.concert.queue.scheduler;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.entity.ReservationPhase;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.scheduler.MoveToReservationPhaseFromWaitQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class MoveToReservationPhaseFromWaitQueueIT {

    @Autowired
    private MoveToReservationPhaseFromWaitQueue moveToReservationPhaseFromWaitQueue;

    @Autowired
    private WaitQueueRepository waitQueueRepository;

    @Autowired
    private ReservationPhaseRepository reservationPhaseRepository;

    @Autowired
    private QueuePositionTrackerRepository queuePositionTrackerRepository;

    @Test
    @DisplayName("대기열에서 예약페이즈로 정상적으로 이동")
    void shouldMoveToReservationPhaseSuccessfully() {
        // given
        Long concertId = 999L;

        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertScheduleId(concertId)
                .waitQueueId(0L)
                .isWaitQueueExist(true)
                .build();
        queuePositionTrackerRepository.save(queuePositionTracker);

        List<WaitQueue> waitQueues = List.of(
                WaitQueue.builder()
                        .concertScheduleId(concertId)
                        .userId(1L)
                        .uuid("uuid1")
                        .build(),
                WaitQueue.builder()
                        .concertScheduleId(concertId)
                        .userId(2L)
                        .uuid("uuid2")
                        .build()
        );
        waitQueueRepository.saveAll(waitQueues);

        // when
        moveToReservationPhaseFromWaitQueue.execute();

        // then
        assertEquals(2, reservationPhaseRepository.countByConcertScheduleId(concertId));

        QueuePositionTracker updatedIndicator = queuePositionTrackerRepository.findByConcertScheduleId(concertId);
        assertNotNull(updatedIndicator);
        assertFalse(updatedIndicator.getIsWaitQueueExist());  // 대기열이 비워졌으므로 false로 변경되었는지 확인
    }

    @Test
    @DisplayName("예약 페이즈가 꽉 차있을 때 대기열에서 이동하지 않음")
    void shouldNotMoveWhenReservationPhaseIsFull() {
        // given
        Long concertId = 999L;

        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertScheduleId(concertId)
                .waitQueueId(0L)
                .isWaitQueueExist(true)
                .build();
        queuePositionTrackerRepository.save(queuePositionTracker);

        // 예약 페이즈에 30명 꽉 찬 상태로 저장
        List<ReservationPhase> reservationPhases = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            reservationPhases.add(
                    ReservationPhase.builder()
                    .concertScheduleId(concertId)
                    .userId((long) i + 1)
                    .uuid("uuid" + (i + 1))
                    .build()
            );
        }
        reservationPhaseRepository.saveAll(reservationPhases);

        WaitQueue waitQueue1 = WaitQueue.builder()
                .concertScheduleId(concertId)
                .userId(31L)
                .uuid("uuid31")
                .build();
        waitQueueRepository.save(waitQueue1);

        // when
        moveToReservationPhaseFromWaitQueue.execute();

        // then
        assertEquals(30, reservationPhaseRepository.countByConcertScheduleId(concertId));  // 더 이상 추가되지 않음
        QueuePositionTracker updatedIndicator = queuePositionTrackerRepository.findByConcertScheduleId(concertId);
        assertTrue(updatedIndicator.getIsWaitQueueExist());  // 대기열은 여전히 존재
    }

    @Test
    @DisplayName("대기열이 비어있을 때 isWaitQueueExist 플래그를 변경")
    void shouldMarkQueueAsNotExistingWhenQueueIsEmpty() {
        // given
        Long concertId = 999L;

        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertScheduleId(concertId)
                .waitQueueId(0L)
                .isWaitQueueExist(true)
                .build();
        queuePositionTrackerRepository.save(queuePositionTracker);

        WaitQueue waitQueue = WaitQueue.builder()
                .concertScheduleId(concertId)
                .userId(1L)
                .uuid("uuid1")
                .build();

        waitQueueRepository.save(waitQueue);

        // when
        moveToReservationPhaseFromWaitQueue.execute();

        // then
        assertEquals(1, reservationPhaseRepository.countByConcertScheduleId(concertId));

        QueuePositionTracker updatedIndicator = queuePositionTrackerRepository.findByConcertScheduleId(concertId);
        assertNotNull(updatedIndicator);
        assertFalse(updatedIndicator.getIsWaitQueueExist());  // 대기열이 비워졌으므로 isWaitQueueExist는 false가 되어야 함
    }
}

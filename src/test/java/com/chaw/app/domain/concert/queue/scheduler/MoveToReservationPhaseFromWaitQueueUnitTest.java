package com.chaw.app.domain.concert.queue.scheduler;

import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.scheduler.MoveToReservationPhaseFromWaitQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class MoveToReservationPhaseFromWaitQueueUnitTest {

    @Mock
    private ReservationPhaseRepository reservationPhaseRepository;

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @Mock
    private QueuePositionTrackerRepository queuePositionTrackerRepository;

    private MoveToReservationPhaseFromWaitQueue moveToReservationPhaseFromWaitQueue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        moveToReservationPhaseFromWaitQueue = Mockito.spy(new MoveToReservationPhaseFromWaitQueue(
                reservationPhaseRepository,
                waitQueueRepository,
                queuePositionTrackerRepository
        ));
    }

    @Test
    void testExecute_withWaitQueue() {
        // Given
        Long concertScheduleId = 1L;
        QueuePositionTracker queuePositionTracker = new QueuePositionTracker();
        queuePositionTracker.setConcertScheduleId(concertScheduleId);

        when(queuePositionTrackerRepository.findAllByIsWaitQueueExist())
                .thenReturn(Arrays.asList(queuePositionTracker));

        when(reservationPhaseRepository.countByConcertScheduleId(concertScheduleId))
                .thenReturn(10); // 30 - 10 = 20 movableSize

        doNothing().when(moveToReservationPhaseFromWaitQueue).move(concertScheduleId, 20);

        // When
        moveToReservationPhaseFromWaitQueue.execute();

        // Then
        verify(queuePositionTrackerRepository, times(1)).findAllByIsWaitQueueExist();
        verify(reservationPhaseRepository, times(1)).countByConcertScheduleId(concertScheduleId);
        verify(moveToReservationPhaseFromWaitQueue, times(1)).move(concertScheduleId, 20);
    }

    @Test
    void testMove_withAvailableSeats() {
        // Given
        Long concertScheduleId = 1L;
        QueuePositionTracker queuePositionTracker = new QueuePositionTracker();
        queuePositionTracker.setConcertScheduleId(concertScheduleId);
        queuePositionTracker.setWaitQueueId(1L);

        List<WaitQueue> waitQueues = Arrays.asList(
                new WaitQueue(1L, concertScheduleId, 1L, "uuid1"),
                new WaitQueue(2L, concertScheduleId, 2L, "uuid2")
        );

        when(queuePositionTrackerRepository.findByConcertScheduleIdWithLock(concertScheduleId))
                .thenReturn(Optional.of(queuePositionTracker));

        when(waitQueueRepository.findByConcertScheduleIdAndIdGreaterThanOrderByIdAsc(concertScheduleId, 1L))
                .thenReturn(waitQueues);

        // When
        moveToReservationPhaseFromWaitQueue.move(concertScheduleId, 2);

        // Then
        verify(reservationPhaseRepository, times(1)).saveAll(anyList());
        verify(queuePositionTrackerRepository, times(1)).save(queuePositionTracker);
        assertEquals(2L, queuePositionTracker.getWaitQueueId());
        assertFalse(queuePositionTracker.getIsWaitQueueExist());
    }

    @Test
    void testMove_noWaitQueues() {
        // Given
        Long concertScheduleId = 1L;
        QueuePositionTracker queuePositionTracker = new QueuePositionTracker();
        queuePositionTracker.setConcertScheduleId(concertScheduleId);
        queuePositionTracker.setWaitQueueId(1L);

        when(queuePositionTrackerRepository.findByConcertScheduleIdWithLock(concertScheduleId))
                .thenReturn(Optional.of(queuePositionTracker));

        when(waitQueueRepository.findByConcertScheduleIdAndIdGreaterThanOrderByIdAsc(concertScheduleId, 1L))
                .thenReturn(Arrays.asList());

        // When
        moveToReservationPhaseFromWaitQueue.move(concertScheduleId, 2);

        // Then
        verify(reservationPhaseRepository, never()).saveAll(anyList());
        verify(queuePositionTrackerRepository, never()).save(queuePositionTracker);
    }
}

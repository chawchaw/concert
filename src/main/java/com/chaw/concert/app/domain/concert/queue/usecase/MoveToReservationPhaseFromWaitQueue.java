package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.ReservationPhase;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 대기열에서 예약페이즈로 넘어간다.
 */
@Service
public class MoveToReservationPhaseFromWaitQueue {

    private final Integer MAX_RESERVATION_PHASE_SIZE = 30;

    private final ReservationPhaseRepository reservationPhaseRepository;
    private final WaitQueueRepository waitQueueRepository;
    private final QueuePositionTrackerRepository queuePositionTrackerRepository;

    public MoveToReservationPhaseFromWaitQueue(
            ReservationPhaseRepository reservationPhaseRepository,
            WaitQueueRepository waitQueueRepository,
            QueuePositionTrackerRepository queuePositionTrackerRepository
    ) {
        this.reservationPhaseRepository = reservationPhaseRepository;
        this.waitQueueRepository = waitQueueRepository;
        this.queuePositionTrackerRepository = queuePositionTrackerRepository;
    }

    /**
     * 대기열이 있는지 확인한다
     * 입장가능수를 확인한다.(없으면 넘어간다)
     */
    @Scheduled(cron = "0/10 * * * * *")  // 매 10초마다 실행
    public void execute() {
        List<QueuePositionTracker> queuePositionTrackers = queuePositionTrackerRepository.findAllByIsWaitQueueExist();
        for (QueuePositionTracker queuePositionTracker : queuePositionTrackers) {
            Integer reservationPhaseSize = reservationPhaseRepository.countByConcertScheduleId(queuePositionTracker.getConcertScheduleId());
            Integer movableSize = MAX_RESERVATION_PHASE_SIZE - reservationPhaseSize;
            if (movableSize <= 0) {
                continue;
            }
            move(queuePositionTracker.getConcertScheduleId(), movableSize);
        }
    }

    /**
     * 대기열에서 입장가능수만큼 가져온다.
     * 예약페이즈에 추가한다.
     * 대기열 순번을 변경한다.
     * 대기가 없으면 대기열 존재여부를 존재하지 않음으로 변경한다.
     */
    @Transactional
    public void move(Long concertScheduleId, Integer movableSize) {
        Optional<QueuePositionTracker> queuePositionTrackerOptional = queuePositionTrackerRepository.findByConcertScheduleIdWithLock(concertScheduleId);
        QueuePositionTracker queuePositionTracker = queuePositionTrackerOptional.orElse(null);
        if (queuePositionTracker == null) {
            return;
        }

        List<WaitQueue> waitQueues = waitQueueRepository.findByConcertScheduleIdAndIdGreaterThanOrderByIdAsc(
                queuePositionTracker.getConcertScheduleId(),
                queuePositionTracker.getWaitQueueId()
        );
        Integer min = Math.min(waitQueues.size(), movableSize);
        if (min <= 0) {
            return;
        }

        List<WaitQueue> limitedWaitQueues = waitQueues.subList(0, min);
        List<ReservationPhase> reservationPhases = limitedWaitQueues.stream()
                        .map(waitQueue -> ReservationPhase.builder()
                                .userId(waitQueue.getUserId())
                                .concertScheduleId(waitQueue.getConcertScheduleId())
                                .uuid(waitQueue.getUuid())
                                .build())
                        .collect(Collectors.toList());
        reservationPhaseRepository.saveAll(reservationPhases);

        if (limitedWaitQueues.size() > 0) {
            queuePositionTracker.setWaitQueueId(limitedWaitQueues.get(min - 1).getId());
        }
        if (waitQueues.size() <= movableSize) {
            queuePositionTracker.setIsWaitQueueExist(false);
        }
        queuePositionTrackerRepository.save(queuePositionTracker);
    }
}

package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.entity.ReservationPhase;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.exception.UserNotInQueueException;
import com.chaw.concert.app.domain.concert.queue.exception.WaitQueueIndicatorNotExist;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 대기열 순서를 조회한다.
 */
@Service
public class GetWaitQueueStatus {

    private final WaitQueueRepository waitQueueRepository;
    private final QueuePositionTrackerRepository queuePositionTrackerRepository;
    private final ReservationPhaseRepository reservationPhaseRepository;

    public GetWaitQueueStatus(QueuePositionTrackerRepository queuePositionTrackerRepository, WaitQueueRepository waitQueueRepository, ReservationPhaseRepository reservationPhaseRepository) {
        this.queuePositionTrackerRepository = queuePositionTrackerRepository;
        this.waitQueueRepository = waitQueueRepository;
        this.reservationPhaseRepository = reservationPhaseRepository;
    }

    public Output execute(Input input) {
        Optional<ReservationPhase> reservationPhase = reservationPhaseRepository.findByConcertScheduleIdAndUuid(input.concertScheduleId(), input.uuid());
        if (reservationPhase.isPresent()) {
            return new Output(0, true);
        }

        QueuePositionTracker queuePositionTracker = queuePositionTrackerRepository.findByConcertScheduleId(input.concertScheduleId());
        if (queuePositionTracker == null) {
            throw new WaitQueueIndicatorNotExist();
        }

        Boolean waitingUserExist = waitQueueRepository.existsByConcertScheduleIdAndUuid(input.concertScheduleId(), input.uuid());
        if (!waitingUserExist) {
            throw new UserNotInQueueException();
        }

        List<WaitQueue> waitQueues = waitQueueRepository.findByConcertScheduleIdAndIdGreaterThanOrderByIdAsc(input.concertScheduleId(), queuePositionTracker.getWaitQueueId());

        Integer position = -1;
        for (int i = 0; i < waitQueues.size(); i++) {
            if (waitQueues.get(i).getUuid().equals(input.uuid())) {
                position = i + 1;
                break;
            }
        }
        if (position == -1) {
            throw new UserNotInQueueException();
        }

        return new Output(position, false);
    }

    public record Input (
        Long concertScheduleId,
        String uuid
    ) {}

    public record Output (
        Integer queuePosition,
        Boolean isReservationPhase
    ) {}
}

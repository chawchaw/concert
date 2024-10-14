package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.entity.ReservationPhase;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.exception.UserNotInQueueException;
import com.chaw.concert.app.domain.concert.queue.exception.WaitQueueIndicatorNotExist;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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
        Optional<ReservationPhase> reservationPhase = reservationPhaseRepository.findByConcertScheduleIdAndUuid(input.getConcertScheduleId(), input.getUuid());
        if (reservationPhase.isPresent()) {
            return new Output(0, true);
        }

        QueuePositionTracker queuePositionTracker = queuePositionTrackerRepository.findByConcertScheduleId(input.getConcertScheduleId());
        if (queuePositionTracker == null) {
            throw new WaitQueueIndicatorNotExist();
        }

        Boolean waitingUserExist = waitQueueRepository.existsByConcertScheduleIdAndUuid(input.getConcertScheduleId(), input.getUuid());
        if (!waitingUserExist) {
            throw new UserNotInQueueException();
        }

        List<WaitQueue> waitQueues = waitQueueRepository.findByConcertScheduleIdAndIdGreaterThanOrderByIdAsc(input.getConcertScheduleId(), queuePositionTracker.getWaitQueueId());

        Integer position = -1;
        for (int i = 0; i < waitQueues.size(); i++) {
            if (waitQueues.get(i).getUuid().equals(input.getUuid())) {
                position = i + 1;
                break;
            }
        }
        if (position == -1) {
            throw new UserNotInQueueException();
        }
        return new Output(position, false);
    }

    @AllArgsConstructor
    @Builder
    @Getter
    public static class Input {
        private Long concertScheduleId;
        private String uuid;
    }

    @AllArgsConstructor
    @Getter
    public static class Output {
        private Integer queuePosition;
        private Boolean isReservationPhase;
    }
}

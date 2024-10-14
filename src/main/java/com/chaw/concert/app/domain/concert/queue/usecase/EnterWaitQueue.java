package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 대기열에 입장한다.
 */
@Service
public class EnterWaitQueue {

    private final WaitQueueRepository waitQueueRepository;
    private final QueuePositionTrackerRepository queuePositionTrackerRepository;

    public EnterWaitQueue(WaitQueueRepository waitQueueRepository, QueuePositionTrackerRepository queuePositionTrackerRepository) {
        this.waitQueueRepository = waitQueueRepository;
        this.queuePositionTrackerRepository = queuePositionTrackerRepository;
    }

    /**
     * 대기열 순번이 있는지 확인한다.(없으면 생성한다.)
     * 대기열 순번의 대기열존재를 확인한다.(존재하지 않으면 존재함으로 변경한다.)
     * 대기열에 추가한다.
     */
    @Transactional
    public Output execute(Input input) {
        String uuid = UUID.randomUUID().toString();
        WaitQueue waitQueue = WaitQueue.builder()
                .userId(input.getUserId())
                .concertScheduleId(input.getConcertScheduleId())
                .uuid(uuid)
                .build();

        QueuePositionTracker queuePositionTracker = queuePositionTrackerRepository.findByConcertScheduleId(input.concertScheduleId);
        if (queuePositionTracker == null) {
            queuePositionTracker = QueuePositionTracker.builder()
                    .concertScheduleId(input.getConcertScheduleId())
                    .waitQueueId(0L)
                    .isWaitQueueExist(true)
                    .build();
            queuePositionTrackerRepository.save(queuePositionTracker);
        }
        if (!queuePositionTracker.getIsWaitQueueExist()) {
            queuePositionTracker.setIsWaitQueueExist(true);
            queuePositionTrackerRepository.save(queuePositionTracker);
        }

        waitQueueRepository.save(waitQueue);

        return new Output(waitQueue);
    }

    @AllArgsConstructor
    @Builder
    @Getter
    public static class Input {
        private Long userId;
        private Long concertScheduleId;
    }

    @Getter
    @AllArgsConstructor
    public class Output {
        private WaitQueue waitQueue;
    }
}

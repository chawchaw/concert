package com.chaw.concert.app.domain.concert.queue.scheduler;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 통과 스케줄러
 */
@Service
public class PassWaitQueue {

    @Value("${concert.queue.pass.size}")
    private Integer PASS_SIZE;

    private final WaitQueueRepository waitQueueRepository;

    public PassWaitQueue(WaitQueueRepository waitQueueRepository) {
        this.waitQueueRepository = waitQueueRepository;
    }

    /**
     * 스케줄러 주기: 1분
     * 대기열 상태 WAIT 을 id 순으로 오름차순 정렬하여
     * 최대 30개까지 상태를 통과로 변경하고 시간을 기록한다.
     * (변경시간은 만료 스케줄러에서 사용)
     */
    public Output execute() {
        List<WaitQueue> waitQueues = waitQueueRepository.findByStatusByLimit(WaitQueueStatus.WAIT, PASS_SIZE);
        waitQueues.forEach(waitQueue -> {
            waitQueue.pass();
            waitQueueRepository.save(waitQueue);
        });
        return new Output(waitQueues.size());
    }

    public record Output (
        Integer countPass
    ) {}
}

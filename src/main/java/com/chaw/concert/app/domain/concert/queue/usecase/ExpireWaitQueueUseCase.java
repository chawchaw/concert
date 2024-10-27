package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 만료 스케줄러
 */
@AllArgsConstructor
@Service
public class ExpireWaitQueueUseCase {

    private final WaitQueueRepository waitQueueRepository;

    /**
     * 스케줄러 주기: 1분
     * (대기열 상태 PASS && 10분이 경과) 조회
     * 삭제
     */
    public Output execute() {
        LocalDateTime expiredAt = WaitQueue.getExpiredTimeFromNow();
        List<WaitQueue> waitQueues = waitQueueRepository.findByStatusAndUpdatedAtBefore(WaitQueueStatus.PASS, expiredAt);
        waitQueues.forEach(waitQueue -> {
            waitQueueRepository.delete(waitQueue);
        });
        return new Output(waitQueues.size());
    }

    public record Output (
        Integer countExpired
    ) {}
}

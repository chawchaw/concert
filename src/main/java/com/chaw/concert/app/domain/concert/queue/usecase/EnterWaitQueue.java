package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 대기열에 입장 및 대기순서, 상태 조회
 */
@Service
@Slf4j
public class EnterWaitQueue {

    private final WaitQueueRepository waitQueueRepository;

    public EnterWaitQueue(WaitQueueRepository waitQueueRepository) {
        this.waitQueueRepository = waitQueueRepository;
    }

    /**
     * 대기열 순번이 있는지 확인한다
     * 없으면 생성한다
     * 상태가 대기중이면 대기순서를 반환한다
     * 상태와 대기순서를 반환한다
     */
    public Output execute(Input input) {
        WaitQueue waitQueue = waitQueueRepository.findByUserId(input.userId());
        if (waitQueue == null) {
            waitQueue = WaitQueue.builder()
                    .userId(input.userId())
                    .status(WaitQueueStatus.WAIT)
                    .createdAt(LocalDateTime.now())
                    .build();
            waitQueueRepository.save(waitQueue);
            log.info("대기열 입장");
        }

        Long order = -1L;
        if (waitQueue.getStatus() == WaitQueueStatus.WAIT) {
            order = waitQueueRepository.countByStatusAndIdLessThan(WaitQueueStatus.WAIT, waitQueue.getId());
        }

        return new Output(waitQueue.getStatus().name(), waitQueue.getCreatedAt(), waitQueue.getUpdatedAt(), order);
    }

    public record Input (
        Long userId
    ) {}

    public record Output (
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long order
    ) {}
}

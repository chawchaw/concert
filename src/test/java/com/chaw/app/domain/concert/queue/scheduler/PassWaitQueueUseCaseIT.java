package com.chaw.app.domain.concert.queue.scheduler;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.PassWaitQueueUseCase;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class PassWaitQueueUseCaseIT {

    @Autowired
    private PassWaitQueueUseCase passWaitQueueUseCase;

    @Autowired
    private WaitQueueRepository waitQueueRepository;

    @Test
    @DisplayName("대기자가 60명일때 30명만 통과한다")
    void execute_shouldUpdate30WaitQueue() {
        // Given
        Integer waitQueueCount = 60;
        IntStream.range(0, waitQueueCount).forEach(i -> {
            WaitQueue waitQueue = WaitQueue.builder()
                    .userId(Integer.toUnsignedLong(i))
                    .status(WaitQueueStatus.WAIT)
                    .build();
            waitQueueRepository.save(waitQueue);
        });

        // When
        PassWaitQueueUseCase.Output result = passWaitQueueUseCase.execute();

        // Then
        assertEquals(30, result.countPass());
        int remainingWaitCount = waitQueueRepository.countByStatus(WaitQueueStatus.WAIT);
        assertEquals(60 - 30, remainingWaitCount);
    }

    @Test
    @DisplayName("대기자가 20명일때 20명만 통과한다")
    void execute_shouldUpdate20WaitQueue() {
        // Given
        Integer waitQueueCount = 20;
        IntStream.range(0, waitQueueCount).forEach(i -> {
            WaitQueue waitQueue = WaitQueue.builder()
                    .userId(Integer.toUnsignedLong(i))
                    .status(WaitQueueStatus.WAIT)
                    .build();
            waitQueueRepository.save(waitQueue);
        });

        // When
        PassWaitQueueUseCase.Output result = passWaitQueueUseCase.execute();

        // Then
        assertEquals(20, result.countPass());
        int remainingWaitCount = waitQueueRepository.countByStatus(WaitQueueStatus.WAIT);
        assertEquals(0, remainingWaitCount);
    }
}

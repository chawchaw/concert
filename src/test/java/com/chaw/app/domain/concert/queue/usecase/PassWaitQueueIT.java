package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.PassWaitQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
@TestPropertySource(properties = {
    "concert.queue.pass.size=30"
})
public class PassWaitQueueIT {

    @Autowired
    private PassWaitQueue passWaitQueue;

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
        PassWaitQueue.Output result = passWaitQueue.execute();

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
        PassWaitQueue.Output result = passWaitQueue.execute();

        // Then
        assertEquals(20, result.countPass());
        int remainingWaitCount = waitQueueRepository.countByStatus(WaitQueueStatus.WAIT);
        assertEquals(0, remainingWaitCount);
    }
}

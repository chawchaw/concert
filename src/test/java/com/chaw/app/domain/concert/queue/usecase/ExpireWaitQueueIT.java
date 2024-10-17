package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.scheduler.ExpireWaitQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
@TestPropertySource(properties = {
        "concert.queue.expired.minutes=10"
})
public class ExpireWaitQueueIT {

    @Autowired
    private ExpireWaitQueue expireWaitQueue;

    @Autowired
    private WaitQueueRepository waitQueueRepository;

    @Test
    @DisplayName("통과자 50명 중에 20명은 만료되었으면 30명만 남아야 한다")
    void execute_shouldDelete20() {
        // Given
        Integer waitQueueCount = 50;
        IntStream.range(0, waitQueueCount).forEach(i -> {
            LocalDateTime updatedAt = LocalDateTime.now();
            if (i < 20) {
                updatedAt = updatedAt.minusMinutes(11);
            }
            WaitQueue waitQueue = WaitQueue.builder()
                    .userId(Integer.toUnsignedLong(i))
                    .status(WaitQueueStatus.PASS)
                    .updatedAt(updatedAt)
                    .build();
            waitQueueRepository.save(waitQueue);
        });

        // When
        ExpireWaitQueue.Output result = expireWaitQueue.execute();

        // Then
        assertEquals(20, result.countExpired());
        int remainingWaitCount = waitQueueRepository.countByStatus(WaitQueueStatus.PASS);
        assertEquals(50 - 20, remainingWaitCount);
    }

    @Test
    @DisplayName("통과자 10명 모두 만료되었으면 모두 삭제되어야한다")
    void execute_shouldDeleteAll() {
        // Given
        Integer waitQueueCount = 10;
        IntStream.range(0, waitQueueCount).forEach(i -> {
            LocalDateTime updatedAt = LocalDateTime.now();
            WaitQueue waitQueue = WaitQueue.builder()
                    .userId(Integer.toUnsignedLong(i))
                    .status(WaitQueueStatus.PASS)
                    .updatedAt(updatedAt.minusMinutes(11))
                    .build();
            waitQueueRepository.save(waitQueue);
        });

        // When
        ExpireWaitQueue.Output result = expireWaitQueue.execute();

        // Then
        assertEquals(10, result.countExpired());
        int remainingWaitCount = waitQueueRepository.countByStatus(WaitQueueStatus.PASS);
        assertEquals(10 - 10, remainingWaitCount);
    }

}

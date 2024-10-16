package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class EnterWaitQueueIT {

    @Autowired
    private WaitQueueRepository waitQueueRepository;

    @Autowired
    private EnterWaitQueue enterWaitQueue;

    @Test
    void testNewUserEntersQueue() {
        // When
        EnterWaitQueue.Input input = new EnterWaitQueue.Input(1L);
        EnterWaitQueue.Output output = enterWaitQueue.execute(input);

        // Then
        assertEquals("WAIT", output.status());
        assertEquals(0L, output.order());

        // Verify
        WaitQueue savedQueue = waitQueueRepository.findByUserId(1L);
        assertNotNull(savedQueue);
        assertEquals(1L, savedQueue.getUserId());
        assertEquals(WaitQueueStatus.WAIT, savedQueue.getStatus());
    }

    @Test
    void testExistingUserInQueue() {
        // Given: 기존 사용자가 대기열에 있을 때
        WaitQueue existingQueue = WaitQueue.builder()
                .userId(1L)
                .status(WaitQueueStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .build();
        waitQueueRepository.save(existingQueue);

        // When: 대기열 상태 확인
        EnterWaitQueue.Input input = new EnterWaitQueue.Input(1L);
        EnterWaitQueue.Output output = enterWaitQueue.execute(input);

        // Then: 대기열 상태가 WAIT이고, 대기순번이 0이어야 함 (첫 번째 사용자이므로 순번 0)
        assertEquals("WAIT", output.status());
        assertEquals(0L, output.order());
    }

    @Test
    void testQueueOrderWithMultipleUsers() {
        // Given: 여러 사용자가 대기열에 있을 때
        WaitQueue firstUser = WaitQueue.builder()
                .userId(1L)
                .status(WaitQueueStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .build();
        WaitQueue secondUser = WaitQueue.builder()
                .userId(2L)
                .status(WaitQueueStatus.WAIT)
                .createdAt(LocalDateTime.now().plusMinutes(1))
                .build();
        waitQueueRepository.save(firstUser);
        waitQueueRepository.save(secondUser);

        // When: 두 번째 사용자의 대기 상태 확인
        EnterWaitQueue.Input input = new EnterWaitQueue.Input(2L);
        EnterWaitQueue.Output output = enterWaitQueue.execute(input);

        // Then: 대기 순번이 1이어야 함 (두 번째 사용자이므로)
        assertEquals("WAIT", output.status());
        assertEquals(1L, output.order());
    }
}

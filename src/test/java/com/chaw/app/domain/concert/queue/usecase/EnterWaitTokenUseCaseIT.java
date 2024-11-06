package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.ActiveTokenRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitTokenRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitQueueUseCase;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitTokenUseCase;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDateTime;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class EnterWaitTokenUseCaseIT {

    @Autowired
    private ActiveTokenRepository activeTokenRepository;

    @Autowired
    private WaitTokenRepository waitTokenRepository;

    @Autowired
    private EnterWaitTokenUseCase enterWaitTokenUseCase;

    @Test
    void test_status_in_EMPTY() {
        // When
        Long userId = 1L;
        EnterWaitTokenUseCase.Input input = new EnterWaitTokenUseCase.Input(userId);
        EnterWaitTokenUseCase.Output output = enterWaitTokenUseCase.execute(input);

        // Then
        assertEquals("WAIT", output.status());
        assertEquals(0, output.order());

        // Verify
        Boolean isActive = activeTokenRepository.existsByUserId(userId);
        assertEquals(false, isActive);
    }

    @Test
    void test_empty_in_ACTIVE() {
        // When
        Long userId = 1L;
        activeTokenRepository.save(userId, 10);
        EnterWaitTokenUseCase.Input input = new EnterWaitTokenUseCase.Input(userId);
        EnterWaitTokenUseCase.Output output = enterWaitTokenUseCase.execute(input);

        // Then
        assertEquals("ACTIVE", output.status());
        assertEquals(-1, output.order());
    }

    @Test
    void test_empty_in_WAIT() {
        // When
        Long userId = 1L;
        waitTokenRepository.saveWithCurrentTime(userId);
        EnterWaitTokenUseCase.Input input = new EnterWaitTokenUseCase.Input(userId);
        EnterWaitTokenUseCase.Output output = enterWaitTokenUseCase.execute(input);

        // Then
        assertEquals("WAIT", output.status());
        assertEquals(0, output.order());
    }

    @Test
    void test_multi_user_enter() {
        // When
        LongStream.rangeClosed(1, 10).forEach(i -> {
            enterWaitTokenUseCase.execute(new EnterWaitTokenUseCase.Input(Long.valueOf(i)));
        });

        // Then
        Long userId = 5L;
        int countAll = waitTokenRepository.countAll();
        Integer rankByUserId5 = waitTokenRepository.getRankByUserId(userId);
        assertEquals(10, countAll);
        assertEquals(5 - 1, rankByUserId5);
    }
}

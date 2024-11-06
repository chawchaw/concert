package com.chaw.app.domain.concert.queue.scheduler;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.repository.ActiveTokenRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitTokenRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.PassWaitTokenUseCase;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class PassWaitTokenUseCaseIT {

    @Autowired
    private WaitTokenRepository waitTokenRepository;

    @Autowired
    private ActiveTokenRepository activeTokenRepository;

    @Autowired
    private PassWaitTokenUseCase passWaitTokenUseCase;

    @Test
    @DisplayName("대기자가 50명일때 30명만 통과한다")
    void execute_shouldUpdate30WaitQueue() {
        // Given
        LongStream.range(0, 50).forEach(i -> {
            waitTokenRepository.saveWithCurrentTime(i);
        });

        // When
        PassWaitTokenUseCase.Output result = passWaitTokenUseCase.execute();

        // Then
        assertEquals(30, result.countPass());
        int remainingWaitCount = waitTokenRepository.countAll();
        assertEquals(50 - 30, remainingWaitCount);

        long activeTokenCount = activeTokenRepository.countAll();
        assertEquals(30, activeTokenCount);
    }

    @Test
    @DisplayName("대기자가 20명일때 20명 모두 통과한다")
    void execute_shouldUpdate20WaitQueue() {
        // Given
        LongStream.range(0, 20).forEach(i -> {
            waitTokenRepository.saveWithCurrentTime(i);
        });

        // When
        PassWaitTokenUseCase.Output result = passWaitTokenUseCase.execute();

        // Then
        assertEquals(20, result.countPass());

        int remainingWaitCount = waitTokenRepository.countAll();
        assertEquals(0, remainingWaitCount);

        long activeTokenCount = activeTokenRepository.countAll();
        assertEquals(20, activeTokenCount);
    }
}

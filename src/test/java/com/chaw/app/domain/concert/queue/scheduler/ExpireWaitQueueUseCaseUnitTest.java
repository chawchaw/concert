package com.chaw.app.domain.concert.queue.scheduler;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.ExpireWaitQueueUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpireWaitQueueUseCaseUnitTest {

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @InjectMocks
    private ExpireWaitQueueUseCase expireWaitQueueUseCase;

    @Test
    void execute_shouldDeleteExpiredWaitQueues() {
        // Given
        LocalDateTime expiredAt = LocalDateTime.now().minusMinutes(10);
        List<WaitQueue> mockWaitQueues = Arrays.asList(
                WaitQueue.builder().userId(1L).status(WaitQueueStatus.PASS).updatedAt(expiredAt).build(),
                WaitQueue.builder().userId(1L).status(WaitQueueStatus.PASS).updatedAt(expiredAt).build()
        );

        when(waitQueueRepository.findByStatusAndUpdatedAtBefore(any(), any())).thenReturn(mockWaitQueues);

        // When
        ExpireWaitQueueUseCase.Output result = expireWaitQueueUseCase.execute();

        // Then
        assertEquals(2, result.countExpired());
        verify(waitQueueRepository, times(2)).delete(any(WaitQueue.class));
    }
}

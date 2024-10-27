package com.chaw.app.domain.concert.queue.scheduler;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.PassWaitQueueUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassWaitQueueUseCaseUnitTest {

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @InjectMocks
    private PassWaitQueueUseCase passWaitQueueUseCase;

    @Test
    void execute_shouldUpdateWaitQueueStatusToPass() {
        // Given
        List<WaitQueue> mockWaitQueues = Arrays.asList(
                WaitQueue.builder().userId(1L).status(WaitQueueStatus.WAIT).build(),
                WaitQueue.builder().userId(2L).status(WaitQueueStatus.WAIT).build()
        );
        when(waitQueueRepository.findByStatusByLimit(any(), any())).thenReturn(mockWaitQueues);

        // When
        PassWaitQueueUseCase.Output result = passWaitQueueUseCase.execute();

        // Then
        assertEquals(2, result.countPass());

        mockWaitQueues.forEach(waitQueue -> assertEquals(WaitQueueStatus.PASS, waitQueue.getStatus()));
        verify(waitQueueRepository, times(2)).save(any(WaitQueue.class));
    }
}

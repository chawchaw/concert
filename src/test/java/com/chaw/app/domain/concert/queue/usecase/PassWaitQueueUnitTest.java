package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.PassWaitQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PassWaitQueueUnitTest {

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @InjectMocks
    private PassWaitQueue passWaitQueue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldUpdateWaitQueueStatusToPass() {
        // Given
        List<WaitQueue> mockWaitQueues = Arrays.asList(
                WaitQueue.builder().userId(1L).status(WaitQueueStatus.WAIT).build(),
                WaitQueue.builder().userId(2L).status(WaitQueueStatus.WAIT).build()
        );
        when(waitQueueRepository.findByStatusByLimit(any(), any())).thenReturn(mockWaitQueues);

        // When
        PassWaitQueue.Output result = passWaitQueue.execute();

        // Then
        assertEquals(2, result.countPass());

        mockWaitQueues.forEach(waitQueue -> assertEquals(WaitQueueStatus.PASS, waitQueue.getStatus()));
        verify(waitQueueRepository, times(2)).save(any(WaitQueue.class));
    }
}

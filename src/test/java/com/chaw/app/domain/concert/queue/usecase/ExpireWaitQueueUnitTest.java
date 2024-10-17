package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.ExpireWaitQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExpireWaitQueueUnitTest {

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @InjectMocks
    private ExpireWaitQueue expireWaitQueue;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        Field field = ExpireWaitQueue.class.getDeclaredField("EXPIRED_MINUTES");
        field.setAccessible(true);
        field.set(expireWaitQueue, 10);
    }

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
        ExpireWaitQueue.Output result = expireWaitQueue.execute();

        // Then
        assertEquals(2, result.countExpired());
        verify(waitQueueRepository, times(2)).delete(any(WaitQueue.class));
    }
}

package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EnterWaitQueueUnitTest {

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @InjectMocks
    private EnterWaitQueue enterWaitQueue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNewUserEntersQueue() {
        // Given
        when(waitQueueRepository.findByUserId(anyLong())).thenReturn(null);

        // When
        EnterWaitQueue.Input input = new EnterWaitQueue.Input(1L);
        EnterWaitQueue.Output output = enterWaitQueue.execute(input);

        // Then
        verify(waitQueueRepository, times(1)).save(any(WaitQueue.class));
        assertEquals("WAIT", output.status());
        assertEquals(0L, output.order());
    }

    @Test
    void testExistingUserInQueue() {
        // Given
        WaitQueue existingQueue = WaitQueue.builder()
                .id(1L)
                .userId(1L)
                .status(WaitQueueStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .build();
        when(waitQueueRepository.findByUserId(anyLong())).thenReturn(existingQueue);

        when(waitQueueRepository.countByStatusAndIdLessThan(any(WaitQueueStatus.class), anyLong())).thenReturn(5L);

        // When
        EnterWaitQueue.Input input = new EnterWaitQueue.Input(1L);
        EnterWaitQueue.Output output = enterWaitQueue.execute(input);

        // Then
        assertEquals("WAIT", output.status());
        assertEquals(5L, output.order());
    }
}

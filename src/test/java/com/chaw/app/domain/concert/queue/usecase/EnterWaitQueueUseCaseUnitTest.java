package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitQueueUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnterWaitQueueUseCaseUnitTest {

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @InjectMocks
    private EnterWaitQueueUseCase enterWaitQueueUseCase;

    @Test
    void testNewUserEntersQueue() {
        // Given
        when(waitQueueRepository.findByUserId(anyLong())).thenReturn(null);

        // When
        EnterWaitQueueUseCase.Input input = new EnterWaitQueueUseCase.Input(1L);
        EnterWaitQueueUseCase.Output output = enterWaitQueueUseCase.execute(input);

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
        EnterWaitQueueUseCase.Input input = new EnterWaitQueueUseCase.Input(1L);
        EnterWaitQueueUseCase.Output output = enterWaitQueueUseCase.execute(input);

        // Then
        assertEquals("WAIT", output.status());
        assertEquals(5L, output.order());
    }
}

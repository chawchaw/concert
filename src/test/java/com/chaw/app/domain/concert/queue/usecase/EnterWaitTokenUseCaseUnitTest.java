package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.repository.ActiveTokenRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitTokenRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitTokenUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnterWaitTokenUseCaseUnitTest {

    @Mock
    private ActiveTokenRepository activeTokenRepository;

    @Mock
    private WaitTokenRepository waitTokenRepository;

    @InjectMocks
    private EnterWaitTokenUseCase enterWaitTokenUseCase;

    @Test
    void test_status_in_ACTIVE() {
        // Given
        when(activeTokenRepository.existsByUserId(anyLong())).thenReturn(true);

        // When
        EnterWaitTokenUseCase.Input input = new EnterWaitTokenUseCase.Input(1L);
        EnterWaitTokenUseCase.Output output = enterWaitTokenUseCase.execute(input);

        // Then
        verify(waitTokenRepository, times(0)).getRankByUserId(anyLong());
        assertEquals("ACTIVE", output.status());
        assertEquals(-1, output.order());
    }

    @Test
    void test_status_in_WAIT_entered() {
        // Given
        when(activeTokenRepository.existsByUserId(anyLong())).thenReturn(false);
        when(waitTokenRepository.getRankByUserId(anyLong())).thenReturn(1);

        // When
        EnterWaitTokenUseCase.Input input = new EnterWaitTokenUseCase.Input(1L);
        EnterWaitTokenUseCase.Output output = enterWaitTokenUseCase.execute(input);

        // Then
        verify(waitTokenRepository, times(0)).saveWithCurrentTime(anyLong());
        assertEquals("WAIT", output.status());
        assertEquals(1, output.order());
    }

    @Test
    void test_status_in_WAIT_not_entered() {
        // Given
        when(activeTokenRepository.existsByUserId(anyLong())).thenReturn(false);
        when(waitTokenRepository.getRankByUserId(anyLong())).thenReturn(null).thenReturn(1);

        // When
        EnterWaitTokenUseCase.Input input = new EnterWaitTokenUseCase.Input(1L);
        EnterWaitTokenUseCase.Output output = enterWaitTokenUseCase.execute(input);

        // Then
        verify(waitTokenRepository, times(2)).getRankByUserId(anyLong());
        verify(waitTokenRepository, times(1)).saveWithCurrentTime(anyLong());
        assertEquals("WAIT", output.status());
        assertEquals(1, output.order());
    }
}

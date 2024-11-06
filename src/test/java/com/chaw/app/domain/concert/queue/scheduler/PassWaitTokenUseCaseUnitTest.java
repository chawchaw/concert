package com.chaw.app.domain.concert.queue.scheduler;

import com.chaw.concert.app.domain.concert.queue.entity.WaitToken;
import com.chaw.concert.app.domain.concert.queue.repository.ActiveTokenRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitTokenRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.PassWaitTokenUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassWaitTokenUseCaseUnitTest {

    @Mock
    private WaitTokenRepository waitTokenRepository;

    @Mock
    private ActiveTokenRepository activeTokenRepository;

    @InjectMocks
    private PassWaitTokenUseCase passWaitTokenUseCase;

    @Test
    void test_pass_all() {
        // Given
        List<WaitToken> mockWaitTokens = Arrays.asList(
                new WaitToken("1", 1.0),
                new WaitToken("1", 2.0)
        );
        when(waitTokenRepository.getTokensEligibleForPass(anyInt())).thenReturn(mockWaitTokens);
        when(waitTokenRepository.removeTokensBeforeTimeStamp(anyDouble())).thenReturn(0);

        // When
        PassWaitTokenUseCase.Output result = passWaitTokenUseCase.execute();

        // Then
        assertEquals(2, result.countPass());

        verify(activeTokenRepository, times(2)).save(anyLong(), anyInt());
    }

}

package com.chaw.app.domain.common.auth.usecase;

import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.domain.common.auth.usecase.JoinUseCase;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JoinUseCaseUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private JoinUseCase joinUseCaseService;

    @Test
    void testExecute_Success() {
        // given
        String username = "username 1";
        String rawPassword = "password 1";
        String encodedPassword = "encoded password 1";

        JoinUseCase.Input input = new JoinUseCase.Input(username, rawPassword);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // when
        JoinUseCase.Output result = joinUseCaseService.execute(input);

        // then
        assertTrue(result.result());
        assertEquals(username, result.username());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testExecute_UsernameExists() {
        // given
        String username = "existinguser";
        JoinUseCase.Input input = new JoinUseCase.Input(username, "password123");

        when(userRepository.existsByUsername(username)).thenReturn(true);

        // when & then
        BaseException exception = assertThrows(BaseException.class, () -> joinUseCaseService.execute(input));
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());

        verify(userRepository, never()).save(any(User.class));
    }
}

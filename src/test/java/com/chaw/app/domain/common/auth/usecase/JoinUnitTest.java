package com.chaw.app.domain.common.auth.usecase;

import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.domain.common.auth.usecase.Join;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JoinUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private Join joinService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute_Success() {
        // given
        String username = "username 1";
        String rawPassword = "password 1";
        String encodedPassword = "encoded password 1";

        Join.Input input = new Join.Input(username, rawPassword);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // when
        Join.Output result = joinService.execute(input);

        // then
        assertTrue(result.result());
        assertEquals(username, result.username());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testExecute_UsernameExists() {
        // given
        String username = "existinguser";
        Join.Input input = new Join.Input(username, "password123");

        when(userRepository.existsByUsername(username)).thenReturn(true);

        // when & then
        BaseException exception = assertThrows(BaseException.class, () -> joinService.execute(input));
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());

        verify(userRepository, never()).save(any(User.class));
    }
}

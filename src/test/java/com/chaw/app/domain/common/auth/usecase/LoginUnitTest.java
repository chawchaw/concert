package com.chaw.app.domain.common.auth.usecase;

import com.chaw.concert.app.domain.common.auth.usecase.CustomUserDetailsService;
import com.chaw.concert.app.domain.common.auth.usecase.Login;
import com.chaw.concert.app.domain.common.auth.util.JwtUtil;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private Login login;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        // given
        String username = "testuser";
        String password = "password123";
        String token = "test-jwt-token";
        Login.Input input = new Login.Input(username, password);

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);

        // when
        Login.Output output = login.execute(input);

        // then
        assertNotNull(output);
        assertEquals(token, output.token());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(userDetails);
    }

    @Test
    void testLogin_InvalidCredentials() {
        // given
        String username = "testuser";
        String password = "wrongpassword";
        Login.Input input = new Login.Input(username, password);

        doThrow(new BadCredentialsException("Bad credentials")).when(authenticationManager).authenticate(any());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> login.execute(input));
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }
}

package com.chaw.app.domain.common.auth.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.domain.common.auth.usecase.Login;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class LoginIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Login loginService;

    @Test
    void testLogin_Success() {
        // given
        User user = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(user);

        // when
        Login.Input input = new Login.Input("testuser", "password123");
        Login.Output output = loginService.execute(input);

        // then
        assertNotNull(output.token());
    }

    @Test
    void testLogin_InvalidCredentials() {
        // given
        User user = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(user);

        // when
        Login.Input input = new Login.Input("testuser", "wrongpassword");
        BaseException exception = assertThrows(BaseException.class, () -> loginService.execute(input));

        // then
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
    }
}

package com.chaw.app.domain.common.auth.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.domain.common.auth.usecase.LoginUseCase;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestExecutionListeners;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class LoginUseCaseIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginUseCase loginUseCaseService;

    @Test
    void testLogin_Success() {
        // given
        User user = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(user);

        // when
        LoginUseCase.Input input = new LoginUseCase.Input("testuser", "password123");
        LoginUseCase.Output output = loginUseCaseService.execute(input);

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
        LoginUseCase.Input input = new LoginUseCase.Input("testuser", "wrongpassword");
        BaseException exception = assertThrows(BaseException.class, () -> loginUseCaseService.execute(input));

        // then
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
    }
}

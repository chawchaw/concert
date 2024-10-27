package com.chaw.app.domain.common.auth.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.domain.common.auth.usecase.JoinUseCase;
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
public class JoinUseCaseUnitIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JoinUseCase joinUseCaseService;

    @Test
    void testJoinNewUser() {
        // given
        JoinUseCase.Input input = new JoinUseCase.Input("username1", "password1");

        // when
        JoinUseCase.Output output = joinUseCaseService.execute(input);

        // then
        User savedUser = userRepository.findByUsernameOrThrow("username1");
        assertNotNull(savedUser);
        assertEquals("username1", savedUser.getUsername());
        assertTrue(passwordEncoder.matches("password1", savedUser.getPassword()));

        // 회원가입 결과 확인
        assertTrue(output.result());
        assertEquals("username1", output.username());
    }

    @Test
    void testJoinExistingUser() {
        // given
        User existingUser = User.builder()
                .username("existinguser")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(existingUser);

        JoinUseCase.Input input = new JoinUseCase.Input("existinguser", "password456");

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> joinUseCaseService.execute(input));
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        assertEquals(1, userRepository.count());
    }
}

package com.chaw.app.domain.common.auth.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.domain.common.auth.usecase.Join;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class JoinUnitIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Join joinService;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testJoinNewUser() {
        // given
        Join.Input input = new Join.Input("username1", "password1");

        // when
        Join.Output output = joinService.execute(input);

        // then
        User savedUser = userRepository.findByUsername("username1");
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

        Join.Input input = new Join.Input("existinguser", "password456");

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> joinService.execute(input));
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        assertEquals(1, userRepository.count());
    }
}

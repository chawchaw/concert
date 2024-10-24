package com.chaw.concert.app.domain.common.auth.usecase;

import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Join {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Join(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Output execute(Input input) {
        Boolean isExist = userRepository.existsByUsername(input.username());
        if (isExist) {
            throw new BaseException(ErrorType.BAD_REQUEST, "아이디 중복입니다.");
        }

        User user = User.builder()
                .username(input.username())
                .password(passwordEncoder.encode(input.password()))
                .build();
        userRepository.save(user);

        return new Output(true, user.getUsername());
    }

    public record Input(
            String username,
            String password
    ) {}

    public record Output(
            Boolean result,
            String username
    ) {}
}

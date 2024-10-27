package com.chaw.concert.app.domain.common.auth.usecase;

import com.chaw.concert.app.domain.common.auth.util.JwtUtil;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class Login {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public Login(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    public Output execute(Input input) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(input.username(), input.password())
            );
        } catch (AuthenticationException e) {
            throw new BaseException(ErrorType.BAD_REQUEST, "인증정보가 올바르지 않습니다.");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(input.username());
        final String jwt = jwtUtil.generateToken(userDetails);

        return new Output(jwt);
    }

    public record Input(
            String username,
            String password
    ) {}

    public record Output(
            String token
    ) {}
}

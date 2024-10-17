package com.chaw.concert.app.infrastructure.web;

import com.chaw.concert.app.domain.common.user.entity.User;
import com.chaw.concert.app.domain.common.user.exception.UserNotFoundException;
import com.chaw.concert.app.domain.common.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UuidInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public UuidInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uuid = request.getHeader("uuid");

        User user = userRepository.findByUuid(uuid);

        if (user == null) {
            throw new UserNotFoundException();
        }

        request.setAttribute("userId", user.getId());
        return true;
    }
}

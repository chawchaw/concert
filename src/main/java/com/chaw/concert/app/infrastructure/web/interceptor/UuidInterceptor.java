package com.chaw.concert.app.infrastructure.web.interceptor;

import com.chaw.concert.app.domain.common.user.entity.User;
import com.chaw.concert.app.domain.common.user.repository.UserRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
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
            throw new BaseException(ErrorType.NOT_FOUND, "잘못된 UUID 입니다");
        }

        request.setAttribute("userId", user.getId());
        return true;
    }
}

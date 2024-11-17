package com.chaw.concert.app.infrastructure.web.interceptor;

import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.domain.concert.queue.entity.UserNode;
import com.chaw.concert.app.domain.concert.queue.repository.UserNodeRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@AllArgsConstructor
@Component
public class UserNodeInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final UserNodeRepository userNodeRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uuid = request.getHeader("uuid");

        Boolean isActive = getIsActive(uuid);

        if (!isActive) {
            throw new BaseException(ErrorType.CONFLICT, "대기열을 통과하지 않았습니다.");
        }

        return true;
    }

    private Boolean getIsActive(String uuid) {
        User user = userRepository.findByUuid(uuid);
        if (user == null) {
            return null;
        }

        UserNode userNode = userNodeRepository.findByUserId(user.getId());
        return userNode != null && userNode.isActive();
    }
}

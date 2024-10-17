package com.chaw.concert.app.infrastructure.web;

import com.chaw.concert.app.domain.common.user.entity.User;
import com.chaw.concert.app.domain.common.user.repository.UserRepository;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.exception.WaitQueueNotFoundException;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class WaitQueueInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final WaitQueueRepository waitQueueRepository;

    public WaitQueueInterceptor(UserRepository userRepository, WaitQueueRepository waitQueueRepository) {
        this.userRepository = userRepository;
        this.waitQueueRepository = waitQueueRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uuid = request.getHeader("uuid");

        WaitQueue waitQueue = getWaitQueue(uuid);

        if (waitQueue == null) {
            throw new WaitQueueNotFoundException();
        }

        return true;
    }

    private WaitQueue getWaitQueue(String uuid) {
        User user = userRepository.findByUuid(uuid);
        if (user == null) {
            return null;
        }

        WaitQueue waitQueue = waitQueueRepository.findByUserId(user.getId());
        if (waitQueue == null || waitQueue.getStatus() != WaitQueueStatus.PASS) {
            return null;
        }

        return waitQueue;
    }
}

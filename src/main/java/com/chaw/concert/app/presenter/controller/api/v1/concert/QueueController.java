package com.chaw.concert.app.presenter.controller.api.v1.concert;

import com.chaw.concert.app.domain.common.auth.util.SecurityUtil;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitQueue;
import com.chaw.concert.app.presenter.controller.api.v1.concert.dto.EnterWaitQueueOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/queue")
@Tag(name = "WaitQueue", description = "대기열")
public class QueueController {

    private final SecurityUtil securityUtils;
    private final EnterWaitQueue enterWaitQueue;

    public QueueController(SecurityUtil securityUtils, EnterWaitQueue enterWaitQueue) {
        this.securityUtils = securityUtils;
        this.enterWaitQueue = enterWaitQueue;
    }

    @Operation(
            summary = "대기열 조회",
            description = "대기열의 토큰을 발급받고 순서를 조회합니다."
    )
    @PostMapping("/enter")
    @ResponseStatus(HttpStatus.OK)
    public EnterWaitQueueOutput enterWaitQueue() {
        Long userId = securityUtils.getCurrentUserId();
        EnterWaitQueue.Output result = enterWaitQueue.execute(new EnterWaitQueue.Input(userId));
        return EnterWaitQueueOutput.builder()
                .status(result.status())
                .createdAt(result.createdAt())
                .updatedAt(result.updatedAt())
                .order(result.order())
                .build();
    }

}

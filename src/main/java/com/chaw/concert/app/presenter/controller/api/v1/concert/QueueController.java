package com.chaw.concert.app.presenter.controller.api.v1.concert;

import com.chaw.concert.app.domain.common.auth.util.SecurityUtil;
import com.chaw.concert.app.domain.concert.queue.usecase.GetUserNodeUseCase;
import com.chaw.concert.app.presenter.controller.api.v1.concert.dto.UserNodeOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/queue")
@Tag(name = "WaitQueue", description = "대기열")
@RequiredArgsConstructor
@RestController
public class QueueController {

    private final SecurityUtil securityUtils;
    private final GetUserNodeUseCase getUserNodeUseCase;

    @Operation(
            summary = "대기열 조회",
            description = "대기열의 토큰을 발급받고 순서를 조회합니다."
    )
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public UserNodeOutput getUserNode() {
        Long userId = securityUtils.getCurrentUserId();
        GetUserNodeUseCase.Output result = getUserNodeUseCase.execute(new GetUserNodeUseCase.Input(userId));
        return UserNodeOutput.of(result);
    }

}

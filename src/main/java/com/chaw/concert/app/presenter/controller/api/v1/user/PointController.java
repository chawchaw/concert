package com.chaw.concert.app.presenter.controller.api.v1.user;

import com.chaw.concert.app.domain.common.auth.util.SecurityUtil;
import com.chaw.concert.app.domain.common.user.usecase.ChargePointRedissonLockUseCase;
import com.chaw.concert.app.domain.common.user.usecase.GetPointUseCase;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.ChargePointInput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.ChargePointOutput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.GetPointOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/user/point")
@Tag(name = "User Point", description = "사용자 포인트 API")
@RequiredArgsConstructor
@RestController
public class PointController {

    private final SecurityUtil securityUtils;
    private final GetPointUseCase getPointUseCase;
    private final ChargePointRedissonLockUseCase chargePointRedissonLockUseCase;

    @Operation(
            summary = "포인트 조회",
            description = "사용자의 현재 포인트 잔액을 조회합니다"
    )
    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public GetPointOutput getPoint() {
        Long userId = securityUtils.getCurrentUserId();
        GetPointUseCase.Output result = getPointUseCase.execute(new GetPointUseCase.Input(userId));
        return GetPointOutput.of(result);
    }

    @Operation(
            summary = "포인트 충전",
            description = "입력하는 숫자만큼 돈 복사됩니당"
    )
    @PostMapping("/charge")
    @ResponseStatus(HttpStatus.OK)
    public ChargePointOutput chargePoint(
            @RequestBody ChargePointInput chargePointInput
    ) {
        Long userId = securityUtils.getCurrentUserId();
        ChargePointRedissonLockUseCase.Output result = chargePointRedissonLockUseCase.execute(new ChargePointRedissonLockUseCase.Input(userId, chargePointInput.point()));
        return ChargePointOutput.of(result);
    }
}

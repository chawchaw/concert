package com.chaw.concert.app.presenter.controller.api.v1.user;

import com.chaw.concert.app.domain.common.auth.usecase.Join;
import com.chaw.concert.app.domain.common.auth.usecase.Login;
import com.chaw.concert.app.domain.common.auth.util.SecurityUtil;
import com.chaw.concert.app.domain.common.user.usecase.ChargePoint;
import com.chaw.concert.app.domain.common.user.usecase.GetPoint;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/point")
@Tag(name = "User Point", description = "사용자 포인트 API")
public class PointController {

    private final SecurityUtil securityUtils;
    private final GetPoint getPoint;
    private final ChargePoint chargePoint;

    public PointController(SecurityUtil securityUtils, GetPoint getPoint, ChargePoint chargePoint) {
        this.securityUtils = securityUtils;
        this.getPoint = getPoint;
        this.chargePoint = chargePoint;
    }

    @Operation(
            summary = "포인트 조회",
            description = "사용자의 현재 포인트 잔액을 조회합니다"
    )
    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public GetPointOutput getPoint() {
        Long userId = securityUtils.getCurrentUserId();
        GetPoint.Output result = getPoint.execute(new GetPoint.Input(userId));
        return GetPointOutput.builder()
                .point(result.point())
                .build();
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
        ChargePoint.Output result = chargePoint.execute(new ChargePoint.Input(userId, chargePointInput.point()));
        return ChargePointOutput.builder()
                .balance(result.balance())
                .build();
    }
}

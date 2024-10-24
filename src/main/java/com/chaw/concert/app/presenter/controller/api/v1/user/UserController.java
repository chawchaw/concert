package com.chaw.concert.app.presenter.controller.api.v1.user;

import com.chaw.concert.app.domain.common.auth.usecase.Join;
import com.chaw.concert.app.domain.common.auth.usecase.Login;
import com.chaw.concert.app.domain.common.auth.util.SecurityUtil;
import com.chaw.concert.app.domain.common.user.usecase.ChargePoint;
import com.chaw.concert.app.domain.common.user.usecase.GetPoint;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.ChargePointInput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.JoinInput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.LoginInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Point", description = "사용자 포인트 API")
public class UserController {

    private final SecurityUtil securityUtils;
    private final Join join;
    private final Login login;
    private final GetPoint getPoint;
    private final ChargePoint chargePoint;

    public UserController(SecurityUtil securityUtils, Join join, Login login, GetPoint getPoint, ChargePoint chargePoint) {
        this.securityUtils = securityUtils;
        this.join = join;
        this.login = login;
        this.getPoint = getPoint;
        this.chargePoint = chargePoint;
    }

    @Operation(
            summary = "회원가입",
            description = "사용자를 등록 합니다."
    )
    @PostMapping("/auth/join")
    @ResponseStatus(HttpStatus.OK)
    public Join.Output join(
            @RequestBody JoinInput loginInput
    ) {
        return join.execute(new Join.Input(loginInput.username(), loginInput.password()));
    }

    @Operation(
            summary = "로그인",
            description = "서비스 사용을 위해 로그인 합니다."
    )
    @PostMapping("/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public Login.Output login(
            @RequestBody LoginInput loginInput
    ) {
        return login.execute(new Login.Input(loginInput.username(), loginInput.password()));
    }

    @Operation(
            summary = "포인트 조회",
            description = "사용자의 현재 포인트 잔액을 조회합니다"
    )
    @GetMapping("/point")
    @ResponseStatus(HttpStatus.OK)
    public GetPoint.Output getPoint() {
        Long userId = securityUtils.getCurrentUserId();
        return getPoint.execute(new GetPoint.Input(userId));
    }

    @Operation(
            summary = "포인트 충전",
            description = "입력하는 숫자만큼 돈 복사됩니당"
    )
    @PostMapping("/point/charge")
    @ResponseStatus(HttpStatus.CREATED)
    public ChargePoint.Output chargePoint(
            @RequestBody ChargePointInput chargePointInput
    ) {
        Long userId = securityUtils.getCurrentUserId();
        return chargePoint.execute(new ChargePoint.Input(userId, chargePointInput.point()));
    }

}

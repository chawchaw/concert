package com.chaw.concert.app.presenter.controller.api.v1.user;

import com.chaw.concert.app.domain.common.auth.usecase.JoinUseCase;
import com.chaw.concert.app.domain.common.auth.usecase.LoginUseCase;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.JoinInput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.JoinOutput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.LoginInput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.LoginOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증")
public class UserController {

    private final JoinUseCase joinUseCase;
    private final LoginUseCase loginUseCase;

    public UserController(JoinUseCase joinUseCase, LoginUseCase loginUseCase) {
        this.joinUseCase = joinUseCase;
        this.loginUseCase = loginUseCase;
    }

    @Operation(
            summary = "회원가입",
            description = "사용자를 등록 합니다."
    )
    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    public JoinOutput join(
            @RequestBody JoinInput loginInput
    ) {
        JoinUseCase.Output result = joinUseCase.execute(new JoinUseCase.Input(loginInput.username(), loginInput.password()));
        return JoinOutput.of(result);
    }

    @Operation(
            summary = "로그인",
            description = "서비스 사용을 위해 로그인 합니다."
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginOutput login(
            @RequestBody LoginInput loginInput
    ) {
        LoginUseCase.Output result = loginUseCase.execute(new LoginUseCase.Input(loginInput.username(), loginInput.password()));
        return LoginOutput.of(result);
    }
}

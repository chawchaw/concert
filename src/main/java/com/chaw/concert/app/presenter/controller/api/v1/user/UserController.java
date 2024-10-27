package com.chaw.concert.app.presenter.controller.api.v1.user;

import com.chaw.concert.app.domain.common.auth.usecase.Join;
import com.chaw.concert.app.domain.common.auth.usecase.Login;
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

    private final Join join;
    private final Login login;

    public UserController(Join join, Login login) {
        this.join = join;
        this.login = login;
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
        Join.Output result = join.execute(new Join.Input(loginInput.username(), loginInput.password()));
        return JoinOutput.builder()
                .result(result.result())
                .username(result.username())
                .build();
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
        Login.Output result = login.execute(new Login.Input(loginInput.username(), loginInput.password()));
        return LoginOutput.builder()
                .token(result.token())
                .build();
    }
}

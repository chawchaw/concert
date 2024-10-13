package com.chaw.concert.app.presentation.api.v1.user;

import com.chaw.concert.app.presentation.api.v1.user.dto.ChargePointInput;
import com.chaw.concert.app.presentation.api.v1.user.dto.ChargePointOutput;
import com.chaw.concert.app.presentation.api.v1.user.dto.GetPointOutput;
import com.chaw.concert.app.presentation.api.v1.user.dto.RequestTokenOutput;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestTokenOutput requestToken() {
        return new RequestTokenOutput("token");
    }

    @PostMapping("/point")
    @ResponseStatus(HttpStatus.CREATED)
    public ChargePointOutput chargePoint(@RequestBody ChargePointInput input) {
        return new ChargePointOutput(200, 100);
    }

    @GetMapping("/point")
    @ResponseStatus(HttpStatus.OK)
    public GetPointOutput getPoint() {
        return new GetPointOutput(200);
    }

}

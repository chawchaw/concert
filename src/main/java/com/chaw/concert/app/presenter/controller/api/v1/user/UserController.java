package com.chaw.concert.app.presenter.controller.api.v1.user;

import com.chaw.concert.app.domain.common.user.usecase.ChargePoint;
import com.chaw.concert.app.domain.common.user.usecase.GetPoint;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.ChargePointInput;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final GetPoint getPoint;
    private final ChargePoint chargePoint;

    public UserController(GetPoint getPoint, ChargePoint chargePoint) {
        this.getPoint = getPoint;
        this.chargePoint = chargePoint;
    }

    @GetMapping("/point")
    @ResponseStatus(HttpStatus.OK)
    public GetPoint.Output getPoint(
            @Parameter(description = "UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true, in = ParameterIn.HEADER)
            @RequestHeader("uuid") String uuid,
            @RequestAttribute("userId") Long userId
    ) {
        return getPoint.execute(new GetPoint.Input(userId));
    }

    @PostMapping("/point/charge")
    @ResponseStatus(HttpStatus.CREATED)
    public ChargePoint.Output chargePoint(
            @Parameter(description = "UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true, in = ParameterIn.HEADER)
            @RequestHeader("uuid") String uuid,
            @RequestAttribute("userId") Long userId,
            @RequestBody ChargePointInput chargePointInput
    ) {
        return chargePoint.execute(new ChargePoint.Input(userId, chargePointInput.point()));
    }

}

package com.chaw.concert.app.infrastructure.feign.client;

import com.chaw.concert.app.presenter.controller.api.v1.user.dto.ChargePointInput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.ChargePointOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "userClient", url = "${api.host}")
public interface UserFeignClient {
    @PostMapping("/user/point/charge")
    ChargePointOutput chargePoint(
            @RequestHeader("Authorization") String token,
            @RequestBody ChargePointInput chargePointInput
    );
}

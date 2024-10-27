package com.chaw.concert.app.infrastructure.feign.client;

import com.chaw.concert.app.presenter.controller.api.v1.user.dto.LoginInput;
import com.chaw.concert.app.presenter.controller.api.v1.user.dto.LoginOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "authClient", url = "${api.host}")
public interface AuthFeignClient {
    @PostMapping("/auth/login")
    LoginOutput login(@RequestBody LoginInput loginInput);
}

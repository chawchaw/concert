package com.chaw.concert.app.infrastructure.feign.client;

import com.chaw.concert.app.presenter.controller.api.v1.concert.dto.UserNodeOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "queueClient", url = "${api.host}")
public interface QueueFeignClient {
    @PostMapping("/queue/enter")
    UserNodeOutput enter(@RequestHeader("Authorization") String token);
}

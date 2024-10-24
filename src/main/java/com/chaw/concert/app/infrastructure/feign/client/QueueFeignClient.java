package com.chaw.concert.app.infrastructure.feign.client;

import com.chaw.concert.app.presenter.controller.api.v1.concert.dto.EnterWaitQueueOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "queueClient", url = "${api.host}")
public interface QueueFeignClient {
    @PostMapping("/queue/enter")
    EnterWaitQueueOutput enter(@RequestHeader("Authorization") String token);
}

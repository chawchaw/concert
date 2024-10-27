package com.chaw.concert.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.chaw.concert.app.infrastructure.feign.client")
public class FeignClientConfig {
}

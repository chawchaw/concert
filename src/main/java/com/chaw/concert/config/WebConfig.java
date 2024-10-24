package com.chaw.concert.config;

import com.chaw.concert.app.infrastructure.web.interceptor.WaitQueueInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final WaitQueueInterceptor waitQueueInterceptor;

    public WebConfig(WaitQueueInterceptor waitQueueInterceptor) {
        this.waitQueueInterceptor = waitQueueInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(waitQueueInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/**/user/auth/login",
                        "/api/**/concert/queue"
                );
    }
}

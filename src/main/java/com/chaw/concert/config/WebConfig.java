package com.chaw.concert.config;

import com.chaw.concert.app.infrastructure.web.UuidInterceptor;
import com.chaw.concert.app.infrastructure.web.WaitQueueInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UuidInterceptor uuidInterceptor;
    private final WaitQueueInterceptor waitQueueInterceptor;

    public WebConfig(UuidInterceptor uuidInterceptor, WaitQueueInterceptor waitQueueInterceptor) {
        this.uuidInterceptor = uuidInterceptor;
        this.waitQueueInterceptor = waitQueueInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(uuidInterceptor)
                .addPathPatterns("/api/**");

        registry.addInterceptor(waitQueueInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/**/concert/queue");
    }
}

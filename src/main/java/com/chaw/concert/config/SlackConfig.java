package com.chaw.concert.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:config/slack.properties")
public class SlackConfig {
}

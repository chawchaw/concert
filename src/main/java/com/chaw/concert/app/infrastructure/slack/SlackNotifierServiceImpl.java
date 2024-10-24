package com.chaw.concert.app.infrastructure.slack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SlackNotifierServiceImpl implements SlackNotifierService {

    private final RestTemplate restTemplate;

    @Value("${slack.webhook.url}")
    private String SLACK_WEBHOOK_URL;

    public SlackNotifierServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Boolean sendErrorNotificationToSlack(String message) {
        try {
            String payload = String.format("{\"text\":\"%s\"}", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(SLACK_WEBHOOK_URL, HttpMethod.POST, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("슬랙 메시지 작성 실패", response.getBody());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("슬랙 메시지 전송 실패", e);
        }
        return false;
    }
}

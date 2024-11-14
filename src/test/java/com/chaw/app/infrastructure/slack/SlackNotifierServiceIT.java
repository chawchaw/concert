package com.chaw.app.infrastructure.slack;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ConcertApplication.class)
public class SlackNotifierServiceIT {

    @Autowired
    private SlackNotifierService slackNotifierService;

    @Test
    @Disabled
    void testSendNotificationToSlack() {
        String message = "Test error message from SlackNotifierService";
        Boolean result = slackNotifierService.sendNotificationToSlack(message);
        assertTrue(result);
    }
}

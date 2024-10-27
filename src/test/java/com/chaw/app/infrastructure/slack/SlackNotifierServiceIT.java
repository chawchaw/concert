package com.chaw.app.infrastructure.slack;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
public class SlackNotifierServiceIT {

    @Autowired
    private SlackNotifierService slackNotifierService;

    @Test
    @Disabled
    void testSendErrorNotificationToSlack() {
        String message = "Test error message from SlackNotifierService";
        Boolean result = slackNotifierService.sendErrorNotificationToSlack(message);
        assertTrue(result);
    }
}

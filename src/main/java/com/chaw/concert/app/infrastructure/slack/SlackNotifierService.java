package com.chaw.concert.app.infrastructure.slack;

public interface SlackNotifierService {

    Boolean sendNotificationToSlack(String message);
}

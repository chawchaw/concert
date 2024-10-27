package com.chaw.concert.app.infrastructure.slack;

public interface SlackNotifierService {

    Boolean sendErrorNotificationToSlack(String message);
}

package com.chaw.concert.app.infrastructure.kafka;

public class KafkaTopics {
    // 그룹 ID
    public static final String GROUP_ID = "concert-group";

    // 테스트 토픽
    public static final String TEST = "TEST";

    // 예약 토픽
    public static final String CONCERT_RESERVE_TOPIC_DATASTORE = "CONCERT_RESERVE_TOPIC_DATASTORE";
    public static final String CONCERT_RESERVE_TOPIC_SLACK = "CONCERT_RESERVE_TOPIC_SLACK";

    // 결제 토픽
    public static final String CONCERT_PAY_TOPIC_DATASTORE = "CONCERT_PAY_TOPIC_DATASTORE";
    public static final String CONCERT_PAY_TOPIC_SLACK = "CONCERT_PAY_TOPIC_SLACK";
}

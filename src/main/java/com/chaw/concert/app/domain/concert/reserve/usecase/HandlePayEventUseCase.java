package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PayEvent;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@AllArgsConstructor
@Service
public class HandlePayEventUseCase {

    private final ConcertDataPlatformRepository concertDataPlatformRepository;
    private final SlackNotifierService slackNotifierService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void saveOnDataPlatform(PayEvent payEvent) {
        concertDataPlatformRepository.savePay(payEvent.concertScheduleId(), payEvent.ticketId(), payEvent.userId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendMessageBySlack(PayEvent payEvent) {
        String message = String.format("결제 완료: concertScheduleId=%d, ticketId=%d, userId=%d", payEvent.concertScheduleId(), payEvent.ticketId(), payEvent.userId());
        slackNotifierService.sendNotificationToSlack(message);
    }
}

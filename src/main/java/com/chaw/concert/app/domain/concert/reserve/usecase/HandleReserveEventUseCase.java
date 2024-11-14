package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReserveEvent;
import com.chaw.concert.app.infrastructure.slack.SlackNotifierService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@AllArgsConstructor
@Service
public class HandleReserveEventUseCase {

    private final ConcertDataPlatformRepository concertDataPlatformRepository;
    private final SlackNotifierService slackNotifierService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void saveOnDataPlatform(ReserveEvent reserveEvent) {
        concertDataPlatformRepository.saveReserve(reserveEvent.concertScheduleId(), reserveEvent.ticketId(), reserveEvent.userId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendMessageBySlack(ReserveEvent reserveEvent) {
        String message = String.format("예약 완료: concertScheduleId=%d, ticketId=%d, userId=%d", reserveEvent.concertScheduleId(), reserveEvent.ticketId(), reserveEvent.userId());
        slackNotifierService.sendNotificationToSlack(message);
    }
}

package com.chaw.concert.app.presenter.scheduler.concert;

import com.chaw.concert.app.domain.concert.reserve.usecase.ConcertOutboxReTryUseCase;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ConcertOutboxScheduler {

    private final ConcertOutboxReTryUseCase concertOutboxReTryUseCase;

    @Scheduled(fixedRate = 5000)
    public void retry() {
        System.out.println("ConcertOutboxScheduler Retry");
        concertOutboxReTryUseCase.reTrySendToKafka();
    }

}

package com.chaw.concert.app.presenter.scheduler.concert;

import com.chaw.concert.app.domain.concert.reserve.usecase.ExpireReserveUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReserveScheduler {

    private final ExpireReserveUseCase expireReserveUseCase;

    public ReserveScheduler(ExpireReserveUseCase expireReserveUseCase) {
        this.expireReserveUseCase = expireReserveUseCase;
    }

//    @Scheduled(cron = "*/10 * * * * *")
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void expireReserve() {
        System.out.println("QueueScheduler expireReserve");
        expireReserveUseCase.execute();
    }
}

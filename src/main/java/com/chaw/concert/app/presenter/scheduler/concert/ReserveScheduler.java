package com.chaw.concert.app.presenter.scheduler.concert;

import com.chaw.concert.app.domain.concert.reserve.scheduler.ExpireReserve;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReserveScheduler {

    private final ExpireReserve expireReserve;

    public ReserveScheduler(ExpireReserve expireReserve) {
        this.expireReserve = expireReserve;
    }

//    @Scheduled(cron = "*/10 * * * * *")
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void expireReserve() {
        System.out.println("QueueScheduler expireReserve");
        expireReserve.execute();
    }
}

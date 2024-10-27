package com.chaw.concert.app.presenter.scheduler.concert;

import com.chaw.concert.app.domain.concert.queue.usecase.ExpireWaitQueueUseCase;
import com.chaw.concert.app.domain.concert.queue.usecase.PassWaitQueueUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QueueScheduler {

    private final PassWaitQueueUseCase passWaitQueueUseCase;
    private final ExpireWaitQueueUseCase expireWaitQueueUseCase;

    public QueueScheduler(PassWaitQueueUseCase passWaitQueueUseCase, ExpireWaitQueueUseCase expireWaitQueueUseCase) {
        this.passWaitQueueUseCase = passWaitQueueUseCase;
        this.expireWaitQueueUseCase = expireWaitQueueUseCase;
    }

//    @Scheduled(cron = "*/10 * * * * *")
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void passQueue() {
        System.out.println("QueueScheduler passQueue");
        passWaitQueueUseCase.execute();
    }

//    @Scheduled(cron = "*/10 * * * * *")
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void expireQueue() {
        System.out.println("QueueScheduler expireQueue");
        expireWaitQueueUseCase.execute();
    }
}

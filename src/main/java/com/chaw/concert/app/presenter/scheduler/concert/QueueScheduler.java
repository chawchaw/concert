package com.chaw.concert.app.presenter.scheduler.concert;

import com.chaw.concert.app.domain.concert.queue.scheduler.ExpireWaitQueue;
import com.chaw.concert.app.domain.concert.queue.scheduler.PassWaitQueue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QueueScheduler {

    private final PassWaitQueue passWaitQueue;
    private final ExpireWaitQueue expireWaitQueue;

    public QueueScheduler(PassWaitQueue passWaitQueue, ExpireWaitQueue expireWaitQueue) {
        this.passWaitQueue = passWaitQueue;
        this.expireWaitQueue = expireWaitQueue;
    }

//    @Scheduled(cron = "*/10 * * * * *")
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void passQueue() {
        System.out.println("QueueScheduler passQueue");
        passWaitQueue.execute();
    }

//    @Scheduled(cron = "*/10 * * * * *")
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void expireQueue() {
        System.out.println("QueueScheduler expireQueue");
        expireWaitQueue.execute();
    }
}

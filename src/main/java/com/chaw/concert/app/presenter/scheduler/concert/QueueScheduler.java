package com.chaw.concert.app.presenter.scheduler.concert;

import com.chaw.concert.app.domain.concert.queue.usecase.PassWaitTokenUseCase;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class QueueScheduler {

    private final PassWaitTokenUseCase passWaitTokenUseCase;

//    @Scheduled(cron = "*/10 * * * * *")
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void passQueue() {
        System.out.println("QueueScheduler passQueue");
        passWaitTokenUseCase.execute();
    }

}

package com.chaw.concert.app.presenter.scheduler.concert;

import com.chaw.concert.app.domain.concert.queue.usecase.ActiveUserNodesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserNodeScheduler {

    private final ActiveUserNodesUseCase activeUserNodesUseCase;

    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void active() {
        System.out.println("QueueScheduler Active");
        activeUserNodesUseCase.execute();
    }

}

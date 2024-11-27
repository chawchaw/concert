package com.chaw.concert.app.infrastructure.event.concert.reserve;

import com.chaw.concert.app.domain.concert.reserve.repository.PaidEventRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PaidEventRepositoryImpl implements PaidEventRepository {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void complete(PaidEvent event) {
        eventPublisher.publishEvent(event);
    }
}

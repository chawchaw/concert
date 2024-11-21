package com.chaw.concert.app.infrastructure.event.concert.reserve;

import com.chaw.concert.app.domain.concert.reserve.repository.PaidEventRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PaidEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class PaidEventRepositoryImpl implements PaidEventRepository {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void complete(PaidEvent event) {
        eventPublisher.publishEvent(event);
    }
}

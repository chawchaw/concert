package com.chaw.concert.app.infrastructure.event.concert.reserve;

import com.chaw.concert.app.domain.concert.reserve.repository.PayEventRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PayEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class PayEventRepositoryImpl implements PayEventRepository {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void complete(PayEvent event) {
        eventPublisher.publishEvent(event);
    }
}

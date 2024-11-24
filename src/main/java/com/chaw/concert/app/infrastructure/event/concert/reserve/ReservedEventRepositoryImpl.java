package com.chaw.concert.app.infrastructure.event.concert.reserve;

import com.chaw.concert.app.domain.concert.reserve.repository.ReservedEventRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ReservedEventRepositoryImpl implements ReservedEventRepository {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void complete(ReservedEvent event) {
        eventPublisher.publishEvent(event);
    }
}

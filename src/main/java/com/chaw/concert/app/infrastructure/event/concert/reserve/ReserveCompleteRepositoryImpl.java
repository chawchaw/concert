package com.chaw.concert.app.infrastructure.event.concert.reserve;

import com.chaw.concert.app.domain.concert.reserve.repository.ReserveEventRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReserveEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class ReserveCompleteRepositoryImpl implements ReserveEventRepository {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void complete(ReserveEvent event) {
        eventPublisher.publishEvent(event);
    }
}

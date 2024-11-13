package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TicketRepositoryImpl implements TicketRepository {
    private final TicketJpaRepository repository;

    public TicketRepositoryImpl(TicketJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Ticket findByIdOrThrow(Long id) {
        Ticket ticket = repository.findById(id).orElse(null);
        throwNotFoundException(ticket);
        return ticket;
    }

    @Override
    public List<Ticket> findByConcertScheduleId(Long concertScheduleId) {
        return repository.findByConcertScheduleId(concertScheduleId);
    }

    @Override
    @Cacheable(value = "tickets", key = "#concertScheduleId", unless = "#result == null")
    public List<Ticket> findByConcertScheduleIdWithCache(Long concertScheduleId) {
        return repository.findByConcertScheduleId(concertScheduleId);
    }

    @Override
    public Ticket save(Ticket ticket) {
        return repository.save(ticket);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    private void throwNotFoundException(Ticket ticket) {
        if (ticket == null) {
            throw new BaseException(ErrorType.NOT_FOUND, "없는 티켓입니다.");
        }
    }
}

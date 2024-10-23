package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TicketRepositoryImpl implements TicketRepository {
    private final TicketJpaRepository repository;

    public TicketRepositoryImpl(TicketJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Ticket findById(Long id) {
        Ticket ticket = repository.findById(id).orElse(null);
        throwNotFoundException(ticket);
        return ticket;
    }

    @Override
    public Ticket findByIdWithLock(Long ticketId) {
        Ticket ticket = repository.findByIdWithLock(ticketId);
        throwNotFoundException(ticket);
        return ticket;
    }

    @Override
    public List<Ticket> findByConcertScheduleIdAndStatus(Long concertScheduleId, TicketStatus status) {
        return repository.findByConcertScheduleIdAndStatus(concertScheduleId, status);
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

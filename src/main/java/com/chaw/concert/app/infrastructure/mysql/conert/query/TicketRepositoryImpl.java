package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.infrastructure.exception.BaseException;
import com.chaw.concert.app.infrastructure.exception.ErrorType;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TicketRepositoryImpl implements TicketRepository {
    private final TicketJpaRepository repository;

    public TicketRepositoryImpl(TicketJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Ticket findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BaseException(ErrorType.NOT_FOUND, "Ticket not found"));
    }

    @Override
    public Ticket findByIdWithLock(Long ticketId) {
        Ticket ticket = repository.findByIdWithLock(ticketId);
        if (ticket == null) {
            throw new BaseException(ErrorType.NOT_FOUND, "Ticket not found");
        }
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
}

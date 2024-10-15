package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TicketRepositoryImpl implements TicketRepository {
    private final TicketJpaRepository repository;

    public TicketRepositoryImpl(TicketJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Ticket> findByIdWithLock(Long ticketId) {
        return repository.findByIdWithLock(ticketId);
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
    public Optional<Ticket> findById(Long id) {
        return repository.findById(id);
    }
}

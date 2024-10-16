package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
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
        return repository.findById(id).orElse(null);
    }

    @Override
    public Ticket findByIdWithLock(Long ticketId) {
        return repository.findByIdWithLock(ticketId);
    }

    @Override
    public List<Ticket> findByConcertScheduleIdAndStatus(Long concertScheduleId, TicketStatus status) {
        return repository.findByConcertScheduleIdAndStatus(concertScheduleId, status);
    }

    @Override
    public List<Ticket> findByReserveExpired() {
        return repository.findByReserveEndAtBeforeAndStatus(LocalDateTime.now(), TicketStatus.RESERVE);
    }

    @Override
    public Ticket save(Ticket ticket) {
        return repository.save(ticket);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}

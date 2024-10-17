package com.chaw.concert.app.domain.concert.query.repository;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {

    Ticket findById(Long id);

    Ticket findByIdWithLock(Long id);

    List<Ticket> findByConcertScheduleIdAndStatus(Long concertScheduleId, TicketStatus ticketStatus);

    Ticket save(Ticket ticket);

    void deleteById(Long id);

    void deleteAll();
}

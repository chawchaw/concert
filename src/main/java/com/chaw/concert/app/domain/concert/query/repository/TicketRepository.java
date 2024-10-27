package com.chaw.concert.app.domain.concert.query.repository;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;

import java.util.List;

public interface TicketRepository {

    Ticket findByIdOrThrow(Long id);

    Ticket findByIdWithLockOrThrow(Long id);

    List<Ticket> findByConcertScheduleIdAndStatus(Long concertScheduleId, TicketStatus ticketStatus);

    Ticket save(Ticket ticket);

    void deleteAll();
}

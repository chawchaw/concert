package com.chaw.concert.app.domain.concert.query.repository;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;

import java.util.List;

public interface TicketRepository {

    Ticket findByIdOrThrow(Long id);

    List<Ticket> findByConcertScheduleId(Long concertScheduleId);

    List<Ticket> findByConcertScheduleIdWithCache(Long concertScheduleId);

    Ticket save(Ticket ticket);

    void deleteAll();
}

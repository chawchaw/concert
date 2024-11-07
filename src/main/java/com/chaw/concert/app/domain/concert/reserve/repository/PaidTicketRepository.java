package com.chaw.concert.app.domain.concert.reserve.repository;

public interface PaidTicketRepository {

    void save(Long concertScheduleId, Long ticketId);

    int countByConcertScheduleId(Long concertScheduleId);
}

package com.chaw.concert.app.domain.concert.reserve.repository;

import java.util.Set;

public interface PaidTicketRepository {

    void save(Long concertScheduleId, Long ticketId);

    int countByConcertScheduleId(Long concertScheduleId);

    Set<Long> findByConcertScheduleId(Long concertScheduleId);
}

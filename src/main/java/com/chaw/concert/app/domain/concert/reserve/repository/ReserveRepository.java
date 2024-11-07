package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;

import java.util.Set;

public interface ReserveRepository {

    Boolean existsByConcertScheduleIdAndTicketIdAndUserId(Long concertScheduleId, Long ticketId, Long userId);

    void save(Reserve reserve);

    Set<Long> findByConcertScheduleId(Long concertScheduleId);
}

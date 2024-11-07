package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ReserveRepository {

    Reserve findByIdOrThrow(Long id);

    Reserve save(Reserve reserve);

    void deleteAll();

    Reserve findByUserIdAndTicketIdOrderByIdDescLimitOrThrow(Long userId, Long ticketId, Integer limit);
}

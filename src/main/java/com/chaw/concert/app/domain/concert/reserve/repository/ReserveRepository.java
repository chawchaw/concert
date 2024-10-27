package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ReserveRepository {

    Reserve findByIdOrThrow(Long id);

    List<Reserve> findByReserveStatusAndCreatedAtBefore(ReserveStatus reserveStatus, LocalDateTime expiredAt);

    Reserve save(Reserve reserve);

    void deleteAll();

    Reserve findByTicketIdOrThrow(Long ticketId);

    Reserve findByUserIdAndTicketIdOrderByIdDescLimitOrThrow(Long userId, Long ticketId, Integer limit);
}

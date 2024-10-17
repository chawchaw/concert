package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ReserveRepository {

    Reserve findById(Long id);

    List<Reserve> findByReserveStatusAndCreatedAtBefore(ReserveStatus reserveStatus, LocalDateTime expiredAt);

    Reserve save(Reserve reserve);

    void deleteAll();

    Reserve findByTicketId(Long ticketId);

    Reserve findByUserIdAndTicketIdOrderByIdDescLimit(Long userId, Long ticketId, Integer limit);
}

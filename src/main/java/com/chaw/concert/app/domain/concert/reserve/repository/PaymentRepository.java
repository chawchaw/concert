package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.entity.Payment;

public interface PaymentRepository {
    Boolean existsByTicketId(Long ticketId);

    void save(Payment payment);

    Integer countByTicketId(Long id);
}

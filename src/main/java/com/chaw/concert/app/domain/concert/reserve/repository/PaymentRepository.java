package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.entity.Payment;

public interface PaymentRepository {
    void save(Payment payment);

    void deleteAll();

    Payment findById(Long id);

    Integer countByReserveId(Long reserveId);
}

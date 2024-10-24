package com.chaw.concert.app.infrastructure.mysql.conert.reserve;

import com.chaw.concert.app.domain.concert.reserve.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Integer countByReserveId(Long reserveId);
}

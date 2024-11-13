package com.chaw.concert.app.infrastructure.mysql.conert.reserve;

import com.chaw.concert.app.domain.concert.reserve.entity.Payment;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository repository;

    public PaymentRepositoryImpl(PaymentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Boolean existsByTicketId(Long ticketId) {
        return repository.existsByTicketId(ticketId);
    }

    @Override
    public void save(Payment payment) {
        repository.save(payment);
    }

    @Override
    public Integer countByTicketId(Long id) {
        return repository.countByTicketId(id);
    }

}

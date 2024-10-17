package com.chaw.concert.app.infrastructure.mysql.conert.reserve;

import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReserveRepositoryImpl implements ReserveRepository {

    private final ReserveJpaRepository repository;

    public ReserveRepositoryImpl(ReserveJpaRepository reserveJpaRepository) {
        this.repository = reserveJpaRepository;
    }

    @Override
    public Reserve findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<Reserve> findByReserveStatusAndCreatedAtBefore(ReserveStatus reserveStatus, LocalDateTime expiredAt) {
        return repository.findByReserveStatusAndCreatedAtBefore(reserveStatus, expiredAt);
    }

    @Override
    public Reserve save(Reserve reserve) {
        return repository.save(reserve);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public Reserve findByTicketId(Long ticketId) {
        return repository.findByTicketId(ticketId);
    }
}

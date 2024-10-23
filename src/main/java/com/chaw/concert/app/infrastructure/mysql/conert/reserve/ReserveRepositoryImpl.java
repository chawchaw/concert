package com.chaw.concert.app.infrastructure.mysql.conert.reserve;

import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.infrastructure.exception.BaseException;
import com.chaw.concert.app.infrastructure.exception.ErrorType;
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
        return repository.findById(id).orElseThrow(() -> new BaseException(ErrorType.NOT_FOUND, "Reserve not found"));
    }

    @Override
    public Reserve findByTicketId(Long ticketId) {
        Reserve reserve = repository.findByTicketId(ticketId);
        if (reserve == null) {
            throw new BaseException(ErrorType.NOT_FOUND, "Reserve not found");
        }
        return reserve;
    }

    @Override
    public Reserve findByUserIdAndTicketIdOrderByIdDescLimit(Long userId, Long ticketId, Integer limit) {
        Reserve reserve = repository.findByUserIdAndTicketIdOrderByIdDescLimit(userId, ticketId, limit);
        if (reserve == null) {
            throw new BaseException(ErrorType.NOT_FOUND, "Reserve not found");
        }
        return reserve;
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
}

package com.chaw.concert.app.infrastructure.redis.concert;

import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class ReserveRepositoryImpl implements ReserveRepository {

    @Override
    public Reserve findByIdOrThrow(Long id) {
        return null;
    }

    @Override
    public Reserve save(Reserve reserve) {
        return null;
    }

    @Override
    public void deleteAll() {

    }

    @Override
    public Reserve findByUserIdAndTicketIdOrderByIdDescLimitOrThrow(Long userId, Long ticketId, Integer limit) {
        return null;
    }
}

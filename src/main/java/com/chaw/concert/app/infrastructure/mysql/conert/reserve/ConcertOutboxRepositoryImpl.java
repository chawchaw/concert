package com.chaw.concert.app.infrastructure.mysql.conert.reserve;

import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxStatus;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxType;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertOutboxRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Repository
public class ConcertOutboxRepositoryImpl implements ConcertOutboxRepository {

    private final ConcertOutboxJpaRepository repository;

    @Override
    public void save(ConcertOutbox concertOutbox) {
        repository.save(concertOutbox);
    }

    @Override
    public ConcertOutbox findByIdOrThrow(Long id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public ConcertOutbox findByIdAndTypeOrThrow(Long id, ConcertOutboxType concertOutboxType) {
        ConcertOutbox concertOutbox = repository.findByIdAndType(id, concertOutboxType);
        if (concertOutbox == null) {
            throw new BaseException(ErrorType.NOT_FOUND, "존재하지 않는 예약 이벤트입니다.");
        }
        return concertOutbox;
    }

    // status 가 INIT 혹은 RETRY 이고 5분 이상 지난 이벤트를 찾아 재시도
    @Override
    public List<ConcertOutbox> findAllByStatusInAndCreatedAtBefore(List<ConcertOutboxStatus> statuses, LocalDateTime before) {
        return repository.findAllByStatusInAndCreatedAtBefore(statuses, before);
    }

    @Override
    public List<ConcertOutbox> findAll() {
        return repository.findAll();
    }

}

package com.chaw.concert.app.infrastructure.dataplatform.concert;

import com.chaw.concert.app.domain.concert.reserve.repository.ConcertDataPlatformRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class ConcertDataPlatformRepositoryImpl implements ConcertDataPlatformRepository {
    @Override
    public void saveReserve(Long concertScheduleId, Long ticketId, Long userId) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("[데이터 전송 완료] 콘서트 예약: concertScheduleId={}, ticketId={}, userId={}", concertScheduleId, ticketId, userId);
    }

    @Override
    public void savePay(Long concertScheduleId, Long ticketId, Long userId) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("[데이터 전송 완료] 콘서트 결제: concertScheduleId={}, ticketId={}, userId={}", concertScheduleId, ticketId, userId);
    }
}

package com.chaw.concert.app.domain.concert.reserve.repository;

public interface ConcertDataPlatformRepository {
    void saveReserve(Long concertScheduleId, Long ticketId, Long userId);

    void savePay(Long concertScheduleId, Long ticketId, Long userId);
}

package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Component
public class ExpireReserveUseCase {

    private final WaitQueueRepository waitQueueRepository;
    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;

    /**
     * if (예약상태 && 마감기간이 지남 in 예약)
     * 티켓 -> UPDATE (EMPTY, reserveUserId null)
     * 예약 -> UPDATE (CANCEL)
     * 대기열 -> 삭제
     */
    public void execute() {
        LocalDateTime expiredAt = Reserve.getExpiredTimeFromNow();
        List<Reserve> reserves = reserveRepository.findByReserveStatusAndCreatedAtBefore(
                ReserveStatus.RESERVE,
                expiredAt);
        reserves.forEach(this::cancelReserve);
    }

    @Transactional
    public void cancelReserve(Reserve reserve) {
        Long userId = reserve.getUserId();
        Ticket ticket = ticketRepository.findByIdOrThrow(reserve.getTicketId());
        ticket.resetToEmpty();
        ticketRepository.save(ticket);

        reserve.cancel();
        reserveRepository.save(reserve);

        WaitQueue waitQueue = waitQueueRepository.findByUserId(userId);
        waitQueueRepository.delete(waitQueue);
    }
}

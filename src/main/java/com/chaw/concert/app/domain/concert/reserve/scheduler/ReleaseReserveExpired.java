package com.chaw.concert.app.domain.concert.reserve.scheduler;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReleaseReserveExpired {

    private final TicketRepository ticketRepository;

    public ReleaseReserveExpired(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * 예약 마감기간이 지난 티켓들을 확인하여 예약을 해제한다.
     */
    @Scheduled(cron = "0/10 * * * * *")  // 매 10초마다 실행
    public void execute() {
        List<Ticket> tickets = ticketRepository.findByReserveExpired();
        tickets.forEach(ticket -> {
            ticket.setReserveEndAt(null);
            ticket.setReserveUserId(null);
            ticketRepository.save(ticket);
        });
    }
}

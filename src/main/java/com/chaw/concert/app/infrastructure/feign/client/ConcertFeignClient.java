package com.chaw.concert.app.infrastructure.feign.client;

import com.chaw.concert.app.presenter.controller.api.v1.concert.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "concertClient", url = "${api.host}")
public interface ConcertFeignClient {
    @GetMapping("/concert")
    GetConcertsOutput getConcerts(@RequestHeader("Authorization") String token);

    @GetMapping("/concert/{concertId}/schedule")
    GetConcertSchedulesNotSoldOutOutput getSchedules(
            @RequestHeader("Authorization") String token,
            @PathVariable Long concertId
    );

    @GetMapping("/concert/{concertId}/schedule/{scheduleId}/tickets")
    GetTicketsInEmptyStatusOutput getTickets(@RequestHeader("Authorization") String token, @PathVariable Long concertId, @PathVariable Long scheduleId);

    @PostMapping("/concert/{concertId}/schedule/{scheduleId}/tickets/{ticketId}/reserve")
    RequestReserveOutput reserveTicket(@RequestHeader("Authorization") String token, @PathVariable Long concertId, @PathVariable Long scheduleId, @PathVariable Long ticketId);

    @PostMapping("/concert/{concertId}/schedule/{scheduleId}/tickets/{ticketId}/pay")
    PayTicketOutput payTicket(@RequestHeader("Authorization") String token, @PathVariable Long concertId, @PathVariable Long scheduleId, @PathVariable Long ticketId);
}

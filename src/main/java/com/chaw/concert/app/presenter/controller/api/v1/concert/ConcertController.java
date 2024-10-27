package com.chaw.concert.app.presenter.controller.api.v1.concert;

import com.chaw.concert.app.domain.common.auth.util.SecurityUtil;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesNotSoldOutUseCase;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertsUseCase;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsInEmptyStatusUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicketUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserveUseCase;
import com.chaw.concert.app.presenter.controller.api.v1.concert.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/concert")
@Tag(name = "Concert", description = "콘서트")
public class ConcertController {

    private final SecurityUtil securityUtils;
    private final GetConcertsUseCase getConcertsUseCase;
    private final GetConcertSchedulesNotSoldOutUseCase getConcertSchedulesNotSoldOutUseCase;
    private final GetTicketsInEmptyStatusUseCase getTicketsInEmptyStatusUseCase;
    private final RequestReserveUseCase requestReserveUseCase;
    private final PayTicketUseCase payTicketUseCase;

    public ConcertController(SecurityUtil securityUtils, GetConcertsUseCase getConcertsUseCase, GetConcertSchedulesNotSoldOutUseCase getConcertSchedulesNotSoldOutUseCase, GetTicketsInEmptyStatusUseCase getTicketsInEmptyStatusUseCase, RequestReserveUseCase requestReserveUseCase, PayTicketUseCase payTicketUseCase) {
        this.securityUtils = securityUtils;
        this.getConcertsUseCase = getConcertsUseCase;
        this.getConcertSchedulesNotSoldOutUseCase = getConcertSchedulesNotSoldOutUseCase;
        this.getTicketsInEmptyStatusUseCase = getTicketsInEmptyStatusUseCase;
        this.requestReserveUseCase = requestReserveUseCase;
        this.payTicketUseCase = payTicketUseCase;
    }

    @Operation(
            summary = "콘서트 조회",
            description = "콘서트를 조회합니다."
    )
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public GetConcertsOutput getConcerts() {
        GetConcertsUseCase.Output result = getConcertsUseCase.execute();
        return GetConcertsOutput.builder()
                .concerts(result.concerts().stream().map(concert -> GetConcertsOutput.Concert.builder()
                        .id(concert.id())
                        .name(concert.name())
                        .info(concert.info())
                        .artist(concert.artist())
                        .host(concert.host())
                        .build()).toList())
                .build();
    }

    @Operation(
            summary = "예약가능 콘서트 일정 조회",
            description = "예약가능한 티켓이 남아있는 콘서트 일정을 조회합니다."
    )
    @GetMapping("/{concertId}/schedule")
    @ResponseStatus(HttpStatus.OK)
    public GetConcertSchedulesNotSoldOutOutput getSchedules(
            @PathVariable Long concertId
    ) {
        Long userId = securityUtils.getCurrentUserId();
        GetConcertSchedulesNotSoldOutUseCase.Output result = getConcertSchedulesNotSoldOutUseCase.execute(
                new GetConcertSchedulesNotSoldOutUseCase.Input(userId, concertId)
        );
        return GetConcertSchedulesNotSoldOutOutput.builder()
                .id(result.id())
                .name(result.name())
                .info(result.info())
                .artist(result.artist())
                .host(result.host())
                .schedules(result.schedules().stream().map(schedule -> GetConcertSchedulesNotSoldOutOutput.Item.builder()
                        .id(schedule.id())
                        .isSoldOut(schedule.isSoldOut())
                        .totalSeat(schedule.totalSeat())
                        .availableSeat(schedule.availableSeat())
                        .dateConcert(schedule.dateConcert())
                        .build()).toList())
                .build();
    }

    @Operation(
            summary = "예약가능 좌석 조회",
            description = "예약가능한 좌석(티켓)을 조회합니다."
    )
    @GetMapping("/{concertId}/schedule/{concertScheduleId}/tickets")
    @ResponseStatus(HttpStatus.OK)
    public GetTicketsInEmptyStatusOutput getTickets(
            @PathVariable Long concertId,
            @PathVariable Long concertScheduleId
    ) {
        GetTicketsInEmptyStatusUseCase.Output result = getTicketsInEmptyStatusUseCase.execute(
                new GetTicketsInEmptyStatusUseCase.Input(concertId, concertScheduleId)
        );
        return GetTicketsInEmptyStatusOutput.builder()
                .concertScheduleId(result.concertScheduleId())
                .tickets(result.tickets().stream().map(ticket -> GetTicketsInEmptyStatusOutput.Item.builder()
                        .id(ticket.id())
                        .type(ticket.type())
                        .seatNo(ticket.seatNo())
                        .price(ticket.price())
                        .build()).toList())
                .build();
    }

    @Operation(
            summary = "예약하기",
            description = "좌석(티켓)을 예약합니다."
    )
    @PostMapping("/{concertId}/schedule/{concertScheduleId}/tickets/{ticketId}/reserve")
    @ResponseStatus(HttpStatus.OK)
    public RequestReserveOutput reserve(
            @PathVariable Long concertId,
            @PathVariable Long concertScheduleId,
            @PathVariable Long ticketId
    ) {
        Long userId = securityUtils.getCurrentUserId();
        RequestReserveUseCase.Output result = requestReserveUseCase.execute(
                new RequestReserveUseCase.Input(userId, concertId, concertScheduleId, ticketId)
        );
        return RequestReserveOutput.builder()
                .success(result.success())
                .build();
    }

    @Operation(
            summary = "결제하기",
            description = "좌석(티켓)을 결제합니다. 잔액 부족시 충전 후 다시 시도해주세요"
    )
    @PostMapping("/{concertId}/schedule/{concertScheduleId}/tickets/{ticketId}/pay")
    @ResponseStatus(HttpStatus.OK)
    public PayTicketOutput pay(
            @PathVariable Long concertId,
            @PathVariable Long concertScheduleId,
            @PathVariable Long ticketId
    ) {
        Long userId = securityUtils.getCurrentUserId();
        PayTicketUseCase.Output result = payTicketUseCase.execute(
                new PayTicketUseCase.Input(userId, concertId, concertScheduleId, ticketId)
        );
        return PayTicketOutput.builder()
                .success(result.success())
                .paymentId(result.paymentId())
                .balance(result.balance())
                .build();
    }
}

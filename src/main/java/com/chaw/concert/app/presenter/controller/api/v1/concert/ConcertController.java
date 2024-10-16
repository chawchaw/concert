package com.chaw.concert.app.presenter.controller.api.v1.concert;

import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesNotSoldOut;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcerts;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsInEmptyStatus;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitQueue;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicket;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserve;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/concert")
@Tag(name = "Concert", description = "콘서트")
public class ConcertController {

    private final EnterWaitQueue enterWaitQueue;
    private final GetConcerts getConcerts;
    private final GetConcertSchedulesNotSoldOut getConcertSchedulesNotSoldOut;
    private final GetTicketsInEmptyStatus getTicketsInEmptyStatus;
    private final RequestReserve requestReserve;
    private final PayTicket payTicket;

    public ConcertController(EnterWaitQueue enterWaitQueue, GetConcerts getConcerts, GetConcertSchedulesNotSoldOut getConcertSchedulesNotSoldOut, GetTicketsInEmptyStatus getTicketsInEmptyStatus, RequestReserve requestReserve, PayTicket payTicket) {
        this.enterWaitQueue = enterWaitQueue;
        this.getConcerts = getConcerts;
        this.getConcertSchedulesNotSoldOut = getConcertSchedulesNotSoldOut;
        this.getTicketsInEmptyStatus = getTicketsInEmptyStatus;
        this.requestReserve = requestReserve;
        this.payTicket = payTicket;
    }

    @Operation(
            summary = "대기열 조회",
            description = "대기열의 토큰을 발급받고 순서를 조회합니다."
    )
    @PostMapping("/queue")
    @ResponseStatus(HttpStatus.OK)
    public EnterWaitQueue.Output enterWaitQueue(
            @Parameter(description = "UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true, in = ParameterIn.HEADER)
            @RequestHeader("uuid") String uuid,
            @RequestAttribute("userId") Long userId
    ) {
        return enterWaitQueue.execute(
                new EnterWaitQueue.Input(userId)
        );
    }

    @Operation(
            summary = "콘서트 조회",
            description = "콘서트를 조회합니다."
    )
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public GetConcerts.Output getConcerts(
            @Parameter(description = "UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true, in = ParameterIn.HEADER)
            @RequestHeader("uuid") String uuid
    ) {
        return getConcerts.execute();
    }

    @Operation(
            summary = "예약가능 콘서트 일정 조회",
            description = "예약가능한 티켓이 남아있는 콘서트 일정을 조회합니다."
    )
    @GetMapping("/{concertId}/schedule")
    @ResponseStatus(HttpStatus.OK)
    public GetConcertSchedulesNotSoldOut.Output getSchedules(
            @Parameter(description = "UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true, in = ParameterIn.HEADER)
            @RequestHeader("uuid") String uuid,
            @PathVariable Long concertId
    ) {
        return getConcertSchedulesNotSoldOut.execute(
                new GetConcertSchedulesNotSoldOut.Input(concertId)
        );
    }

    @Operation(
            summary = "예약가능 좌석 조회",
            description = "예약가능한 좌석(티켓)을 조회합니다."
    )
    @GetMapping("/{concertId}/schedule/{concertScheduleId}/tickets")
    @ResponseStatus(HttpStatus.OK)
    public GetTicketsInEmptyStatus.Output getTickets(
            @Parameter(description = "UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true, in = ParameterIn.HEADER)
            @RequestHeader("uuid") String uuid,
            @PathVariable Long concertId,
            @PathVariable Long concertScheduleId
    ) {
        return getTicketsInEmptyStatus.execute(
                new GetTicketsInEmptyStatus.Input(concertId, concertScheduleId)
        );
    }

    @Operation(
            summary = "예약하기",
            description = "좌석(티켓)을 예약합니다."
    )
    @PostMapping("/{concertId}/schedule/{concertScheduleId}/tickets/{ticketId}/reserve")
    @ResponseStatus(HttpStatus.OK)
    public RequestReserve.Output reserve(
            @Parameter(description = "UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true, in = ParameterIn.HEADER)
            @RequestHeader("uuid") String uuid,
            @RequestAttribute("userId") Long userId,
            @PathVariable Long concertId,
            @PathVariable Long concertScheduleId,
            @PathVariable Long ticketId
    ) {
        return requestReserve.execute(
                new RequestReserve.Input(userId, concertId, concertScheduleId, ticketId)
        );
    }

    @Operation(
            summary = "결제하기",
            description = "좌석(티켓)을 결제합니다. 잔액 부족시 충전 후 다시 시도해주세요"
    )
    @PostMapping("/{concertId}/schedule/{concertScheduleId}/tickets/{ticketId}/pay")
    @ResponseStatus(HttpStatus.OK)
    public PayTicket.Output pay(
            @Parameter(description = "UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true, in = ParameterIn.HEADER)
            @RequestHeader("uuid") String uuid,
            @RequestAttribute("userId") Long userId,
            @PathVariable Long concertId,
            @PathVariable Long concertScheduleId,
            @PathVariable Long ticketId
    ) {
        return payTicket.execute(
                new PayTicket.Input(userId, concertId, concertScheduleId, ticketId)
        );
    }
}

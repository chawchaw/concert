package com.chaw.concert.app.presenter.controller.api.v1.concert;

import com.chaw.concert.app.domain.common.auth.util.SecurityUtil;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesUseCase;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertsUseCase;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.ReserveUseCase;
import com.chaw.concert.app.presenter.controller.api.v1.concert.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/concert")
@Tag(name = "Concert", description = "콘서트")
@AllArgsConstructor
public class ConcertController {

    private final SecurityUtil securityUtils;
    private final GetConcertsUseCase getConcertsUseCase;
    private final GetConcertSchedulesUseCase getConcertSchedulesUseCase;
    private final GetTicketsUseCase getTicketsUseCase;
    private final ReserveUseCase reserveUseCase;
    private final PayUseCase payUseCase;

    @Operation(
            summary = "콘서트 조회",
            description = "콘서트를 조회합니다."
    )
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public GetConcertsOutput getConcerts() {
        GetConcertsUseCase.Output result = getConcertsUseCase.execute();
        return GetConcertsOutput.of(result);
    }

    @Operation(
            summary = "예약가능 콘서트 일정 조회",
            description = "예약가능한 티켓이 남아있는 콘서트 일정을 조회합니다."
    )
    @GetMapping("/{concertId}/schedule")
    @ResponseStatus(HttpStatus.OK)
    public GetConcertSchedulesNotSoldOutOutput getSchedules(
            @PathVariable Long concertId,
            @RequestBody GetConcertSchedulesInput input
    ) {
        Long userId = securityUtils.getCurrentUserId();
        GetConcertSchedulesUseCase.Output result = getConcertSchedulesUseCase.execute(
                new GetConcertSchedulesUseCase.Input(userId, concertId, input.dateConcertFrom(), input.dateConcertTo())
        );
        return GetConcertSchedulesNotSoldOutOutput.of(result);
    }

    @Operation(
            summary = "예약가능 좌석 조회",
            description = "예약가능한 좌석(티켓)을 조회합니다."
    )
    @GetMapping("/schedule/{concertScheduleId}/tickets")
    @ResponseStatus(HttpStatus.OK)
    public GetTicketsInEmptyStatusOutput getTickets(
            @PathVariable Long concertScheduleId
    ) {
        GetTicketsUseCase.Output result = getTicketsUseCase.execute(
                new GetTicketsUseCase.Input(concertScheduleId)
        );
        return GetTicketsInEmptyStatusOutput.of(result);
    }

    @Operation(
            summary = "예약하기",
            description = "좌석(티켓)을 예약합니다."
    )
    @PostMapping("/tickets/{ticketId}/reserve")
    @ResponseStatus(HttpStatus.OK)
    public RequestReserveOutput reserve(
            @PathVariable Long ticketId
    ) {
        Long userId = securityUtils.getCurrentUserId();
        ReserveUseCase.Output result = reserveUseCase.execute(
                new ReserveUseCase.Input(userId, ticketId)
        );
        return RequestReserveOutput.of(result);
    }

    @Operation(
            summary = "결제하기",
            description = "좌석(티켓)을 결제합니다. 잔액 부족시 충전 후 다시 시도해주세요"
    )
    @PostMapping("/tickets/{ticketId}/pay")
    @ResponseStatus(HttpStatus.OK)
    public PayTicketOutput pay(
            @PathVariable Long ticketId
    ) {
        Long userId = securityUtils.getCurrentUserId();
        PayUseCase.Output result = payUseCase.execute(
                new PayUseCase.Input(userId, ticketId)
        );
        return PayTicketOutput.of(result);
    }
}

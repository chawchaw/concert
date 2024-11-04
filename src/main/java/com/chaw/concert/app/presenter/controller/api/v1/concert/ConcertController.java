package com.chaw.concert.app.presenter.controller.api.v1.concert;

import com.chaw.concert.app.domain.common.auth.util.SecurityUtil;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesNotSoldOutUseCase;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertsUseCase;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsInEmptyStatusUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicketRedissonRLockUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserveRedissonRLockUseCase;
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
    private final GetConcertSchedulesNotSoldOutUseCase getConcertSchedulesNotSoldOutUseCase;
    private final GetTicketsInEmptyStatusUseCase getTicketsInEmptyStatusUseCase;
    private final RequestReserveRedissonRLockUseCase requestReserveRedissonRLockUseCase;
    private final PayTicketRedissonRLockUseCase payTicketRedissonRLockUseCase;

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
            @PathVariable Long concertId
    ) {
        Long userId = securityUtils.getCurrentUserId();
        GetConcertSchedulesNotSoldOutUseCase.Output result = getConcertSchedulesNotSoldOutUseCase.execute(
                new GetConcertSchedulesNotSoldOutUseCase.Input(userId, concertId)
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
        GetTicketsInEmptyStatusUseCase.Output result = getTicketsInEmptyStatusUseCase.execute(
                new GetTicketsInEmptyStatusUseCase.Input(concertScheduleId)
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
        RequestReserveRedissonRLockUseCase.Output result = requestReserveRedissonRLockUseCase.execute(
                new RequestReserveRedissonRLockUseCase.Input(userId, ticketId)
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
        PayTicketRedissonRLockUseCase.Output result = payTicketRedissonRLockUseCase.execute(
                new PayTicketRedissonRLockUseCase.Input(userId, ticketId)
        );
        return PayTicketOutput.of(result);
    }
}

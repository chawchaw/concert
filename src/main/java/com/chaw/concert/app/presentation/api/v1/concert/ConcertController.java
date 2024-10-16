package com.chaw.concert.app.presentation.api.v1.concert;

import com.chaw.concert.app.presentation.api.v1.concert.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/concerts")
public class ConcertController {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public GetConcertsOutput getConcerts(@RequestBody GetConcertsInput input) {
        return new GetConcertsOutput(
                1L,
                "콜드플레이 내한공연",
                "판매 중",
                "LIVE NATION PRESENTS COLDPLAY : MUSIC OF THE SPHERES DELIVERED BY DHL",
                "콜드플레이",
                "인터파크",
                "2025-04-16",
                "2024-09-27",
                "2025-04-15",
                "고양종합운동장 주경기장",
                "경기도 고양시 일산서구 대화동",
                "2320 고양종합운동장 주경기장",
                "37.6757812,126.742595");
    }

    @GetMapping("/{id}/tickets")
    @ResponseStatus(HttpStatus.OK)
    public GetTicketsOutput getTickets(@PathVariable Long id) {
        List<GetTicketsOutput.Item> items = List.of(
                new GetTicketsOutput.Item(
                        1L,
                        "공석",
                        "A",
                        "A32",
                        "VIP",
                        200000),
                new GetTicketsOutput.Item(
                        2L,
                        "예약완료",
                        "B",
                        "B15",
                        "1등석",
                        150000)
        );

        return new GetTicketsOutput(items);
    }

    @PostMapping("/tickets/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TempBookingOutput tempBooking(@PathVariable Long id) {
        return new TempBookingOutput(
                1L,
                "임시예약",
                LocalDateTime.now().plusMinutes(5));
    }

    @PostMapping("/tickets/{id}/pay")
    @ResponseStatus(HttpStatus.OK)
    public PayOutput pay(@PathVariable Long id) {
        return new PayOutput(
                1L,
                "임시예약",
                LocalDateTime.now(),
                1000,
                9000);
    }
}

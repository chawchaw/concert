package com.chaw.concert.interfaces.api.concert;

import com.chaw.concert.interfaces.api.concert.dto.*;
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
        return GetConcertsOutput.builder()
                .id(1L)
                .name("콜드플레이 내한공연")
                .status("판매 중")
                .info("LIVE NATION PRESENTS COLDPLAY : MUSIC OF THE SPHERES DELIVERED BY DHL")
                .artist("콜드플레이")
                .host("인터파크")
                .date("2025-04-16")
                .can_buy_from("2024-09-27")
                .can_buy_to("2025-04-15")
                .hall_name("고양종합운동장 주경기장")
                .hall_address("경기도 고양시 일산서구 대화동")
                .hall_address_detail("2320 고양종합운동장 주경기장")
                .hall_location("37.6757812,126.742595")
                .build();
    }

    @GetMapping("/{id}/tickets")
    @ResponseStatus(HttpStatus.OK)
    public List<GetTicketsOutput> getTickets(@PathVariable Long id) {
        return List.of(
                GetTicketsOutput.builder()
                        .id(1L)
                        .status("공석")
                        .seat_zone("A")
                        .seat_no("A32")
                        .seat_type("VIP")
                        .seat_price(200000)
                        .build(),
                GetTicketsOutput.builder()
                        .id(2L)
                        .status("예약완료")
                        .seat_zone("B")
                        .seat_no("B15")
                        .seat_type("1등석")
                        .seat_price(150000)
                        .build()
        );
    }

    @PostMapping("/tickets/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TempBookingOutput tempBooking(@PathVariable Long id) {
        return TempBookingOutput.builder()
                .id(1L)
                .status("임시예약")
                .temp_booking_end_at(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    @PostMapping("/tickets/{id}/pay")
    @ResponseStatus(HttpStatus.OK)
    public PayOutput pay(@PathVariable Long id) {
        return PayOutput.builder()
                .id(1L)
                .status("임시예약")
                .booked_at(LocalDateTime.now())
                .point_used(1000)
                .point_balance(9000)
                .build();
    }
}

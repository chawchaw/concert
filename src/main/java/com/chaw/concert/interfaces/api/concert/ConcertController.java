package com.chaw.concert.interfaces.api.concert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/concerts")
public class ConcertController {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public Output1 getConcerts(@RequestBody Input1 input) {
        return Output1.builder()
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

    @Data
    @NoArgsConstructor
    public static class Input1 {
        private LocalDateTime from;
        private LocalDateTime to;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class Output1 {
        private Long id;
        private String name;
        private String status;
        private String info;
        private String artist;
        private String host;
        private String date;
        private String can_buy_from;
        private String can_buy_to;
        private String hall_name;
        private String hall_address;
        private String hall_address_detail;
        private String hall_location;
    }

    @GetMapping("/{id}/tickets")
    @ResponseStatus(HttpStatus.OK)
    public List<Output2> getTickets(@PathVariable Long id) {
        return List.of(
                Output2.builder()
                        .id(1L)
                        .status("공석")
                        .seat_zone("A")
                        .seat_no("A32")
                        .seat_type("VIP")
                        .seat_price(200000)
                        .build(),
                Output2.builder()
                        .id(2L)
                        .status("예약완료")
                        .seat_zone("B")
                        .seat_no("B15")
                        .seat_type("1등석")
                        .seat_price(150000)
                        .build()
        );
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class Output2 {
        private Long id;
        private String status;
        private String seat_zone;
        private String seat_no;
        private String seat_type;
        private Integer seat_price;
    }

    @PostMapping("/tickets/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Output3 tempBooking(@PathVariable Long id) {
        return Output3.builder()
                .id(1L)
                .status("임시예약")
                .temp_booking_end_at(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class Output3 {
        private Long id;
        private String status;
        private LocalDateTime temp_booking_end_at;
    }

    @PostMapping("/tickets/{id}/pay")
    @ResponseStatus(HttpStatus.OK)
    public Output4 pay(@PathVariable Long id) {
        return Output4.builder()
                .id(1L)
                .status("임시예약")
                .booked_at(LocalDateTime.now())
                .point_used(1000)
                .point_balance(9000)
                .build();
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class Output4 {
        private Long id;
        private String status;
        private LocalDateTime booked_at;
        private Integer point_used;
        private Integer point_balance;
    }
}

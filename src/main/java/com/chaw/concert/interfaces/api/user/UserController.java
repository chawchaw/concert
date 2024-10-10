package com.chaw.concert.interfaces.api.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.CREATED)
    public Output1 requestToken() {
        return new Output1("token");
    }

    @Data
    @AllArgsConstructor
    public static class Output1 {
        private String token;
    }

    @PostMapping("/point")
    @ResponseStatus(HttpStatus.CREATED)
    public Output2 chargePoint(@RequestBody Input2 input) {
        return new Output2(200, 100);
    }

    @Data
    @NoArgsConstructor
    public static class Input2 {
        private Integer point;
    }

    @Data
    @AllArgsConstructor
    public static class Output2 {
        private Integer balance;
        private Integer point;
    }


    @GetMapping("/point")
    @ResponseStatus(HttpStatus.OK)
    public Output3 getPoint() {
        return new Output3(200);
    }

    @Data
    @AllArgsConstructor
    public static class Output3 {
        private Integer balance;
    }
}

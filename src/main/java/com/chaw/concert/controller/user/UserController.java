package com.chaw.concert.controller.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class UserController {

    private final AtomicLong counter = new AtomicLong();

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Output createUser(@RequestBody Input input) {
        long userId = counter.incrementAndGet();
        return new Output(userId, input.getUsername());
    }

    @Data
    @NoArgsConstructor
    public static class Input {
        private String username;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class Output {
        private long id;
        private String username;
    }
}

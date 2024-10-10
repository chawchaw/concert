package com.chaw.concert.interfaces.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestTokenOutput {
    private String token;
}

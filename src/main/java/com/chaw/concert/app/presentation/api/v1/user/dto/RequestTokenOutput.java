package com.chaw.concert.app.presentation.api.v1.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestTokenOutput {
    private String token;
}

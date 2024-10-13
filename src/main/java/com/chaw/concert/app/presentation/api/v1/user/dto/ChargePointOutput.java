package com.chaw.concert.app.presentation.api.v1.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChargePointOutput {
    private Integer balance;
    private Integer point;
}

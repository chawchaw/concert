package com.chaw.concert.interfaces.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChargePointOutput {
    private Integer balance;
    private Integer point;
}

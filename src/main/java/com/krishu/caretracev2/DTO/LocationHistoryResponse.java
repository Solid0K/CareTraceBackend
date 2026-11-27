package com.krishu.caretracev2.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LocationHistoryResponse {
    private Double latitude;
    private Double longitude;
    private LocalDateTime recordedAt;
}

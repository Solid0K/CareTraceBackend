package com.krishu.caretracev2.DTO;

import com.krishu.caretracev2.LocationEventType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LocationEventResponse {
    private String safeZoneId;
    private String zoneName;
    private LocationEventType type;
    private Double latitude;
    private Double longitude;
    private LocalDateTime occurredAt;
}

package com.krishu.caretracev2.Model;

import com.krishu.caretracev2.LocationEventType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection="location_event")
public class LocationEvent {
    @Id
    private String id;
    private String patientId;
    private String safeZoneId;
    private String zoneName;
    private LocationEventType type;
    private Double latitude;
    private Double longitude;
    private LocalDateTime occurredAt;
}

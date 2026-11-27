package com.krishu.caretracev2.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection="location_history")
public class LocationHistory {
    private String id;
    private String patientId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime recordedAt;
}

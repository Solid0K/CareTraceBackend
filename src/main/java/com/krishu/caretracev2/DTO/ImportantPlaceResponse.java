package com.krishu.caretracev2.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ImportantPlaceResponse {
    private String id;
    private String name;
    private String description;
    private String patientId;
    private String photoUrl;
}

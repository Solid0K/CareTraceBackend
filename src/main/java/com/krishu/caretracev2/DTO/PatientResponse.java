package com.krishu.caretracev2.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PatientResponse {
    private String id;
    private String name;
    private Integer age;
    private String email;
    private String photoUrl;
}

package com.krishu.caretracev2.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ImportantPersonResponse {
    private String id;
    private String name;
    private String relation;
    private Long phoneNo;
    private String photoUrl;
}

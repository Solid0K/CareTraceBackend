package com.krishu.caretracev2.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection="ImportantPerson")
public class ImportantPerson {
    @Id
    private String id;
    private String name;
    private String relation;
    private Long phoneNo;
    private String photoKey;
    private String patientId;
}

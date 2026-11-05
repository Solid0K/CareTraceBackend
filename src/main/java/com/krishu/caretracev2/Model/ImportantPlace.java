package com.krishu.caretracev2.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection="ImportantPlace")
public class ImportantPlace {
    @Id
    private String id;

    private String patientId;
    private String name;
    private String description;
    private String photoKey;
}

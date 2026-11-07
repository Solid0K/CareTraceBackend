package com.krishu.caretracev2.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;


@Setter
@Getter
@Document(collection="medications")
public class Medication{
    @Id
    private String id;
    private String patientId;
    private String name;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String instructions;
    private String prescribingDoctor;

    public boolean isActive(LocalDate date){
        boolean startAt=this.startDate==null || !this.startDate.isAfter(date);
        boolean endAt=this.endDate==null || !this.endDate.isBefore(date);
        return startAt && endAt;
    }
}

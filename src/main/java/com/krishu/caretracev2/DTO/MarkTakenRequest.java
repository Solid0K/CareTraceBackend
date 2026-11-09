package com.krishu.caretracev2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkTakenRequest {
    @NotBlank
    @Size(max=500,message="note should not exceed 500 words")
    private String note;
}

package com.krishu.caretracev2.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PlaceGuesserQuestionResponse {
    private String sessionId;
    private Integer questionNo;
    private Integer totalQuestions;
    private String placeId;
    private String photoUrl;
    private List<String> options;
}

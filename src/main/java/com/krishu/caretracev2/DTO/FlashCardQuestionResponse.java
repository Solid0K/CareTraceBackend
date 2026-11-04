package com.krishu.caretracev2.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FlashCardQuestionResponse {
    private String sessionId;
    private String personId;
    private String photoUrl;
    private List<String> options;
    private int questionNumber;
    private int totalQuestions;
}

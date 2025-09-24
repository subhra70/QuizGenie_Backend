package com.subhrashaw.QuizGeneratorBackend.DTO;


import java.util.List;

public class ManualQuizRequest {
    private QuizRequest quizDetails;
    private List<ManualQuizQuestion> questionDetails;

    public QuizRequest getQuizDetails() {
        return quizDetails;
    }

    public void setQuizDetails(QuizRequest quizDetails) {
        this.quizDetails = quizDetails;
    }

    public List<ManualQuizQuestion> getQuestionDetails() {
        return questionDetails;
    }

    public void setQuestionDetails(List<ManualQuizQuestion> questionDetails) {
        this.questionDetails = questionDetails;
    }
}

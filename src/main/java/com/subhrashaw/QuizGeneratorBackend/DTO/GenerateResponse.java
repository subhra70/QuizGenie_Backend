package com.subhrashaw.QuizGeneratorBackend.DTO;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizQuestion;

import java.util.List;

public class GenerateResponse {
    private List<QuizQuestion> questions;
    private int fullMarks;

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public int getFullMarks() {
        return fullMarks;
    }

    public void setFullMarks(int fullMarks) {
        this.fullMarks = fullMarks;
    }
}

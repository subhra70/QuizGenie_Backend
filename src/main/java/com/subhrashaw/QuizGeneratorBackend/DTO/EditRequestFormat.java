package com.subhrashaw.QuizGeneratorBackend.DTO;

import java.util.List;

public class EditRequestFormat {
    private int quizId;
    private int fullMarks;
    private int duration;
    private List<EditQuestionFormat> questionSet;

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getFullMarks() {
        return fullMarks;
    }

    public void setFullMarks(int fullMarks) {
        this.fullMarks = fullMarks;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<EditQuestionFormat> getQuestionSet() {
        return questionSet;
    }

    public void setQuestionSet(List<EditQuestionFormat> questionSet) {
        this.questionSet = questionSet;
    }

    @Override
    public String toString() {
        return "EditRequestFormat{" +
                "quizId=" + quizId +
                ", fullMarks=" + fullMarks +
                ", duration=" + duration +
                ", questionSet=" + questionSet +
                '}';
    }
}

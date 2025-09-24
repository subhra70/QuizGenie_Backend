package com.subhrashaw.QuizGeneratorBackend.DTO;

import java.util.Arrays;

public class QuizResponse {
    private int id;
    private String answer[];

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String[] getAnswer() {
        return answer;
    }

    public void setAnswer(String[] answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "QuizResponse{" +
                "id=" + id +
                ", answer=" + Arrays.toString(answer) +
                '}';
    }
}

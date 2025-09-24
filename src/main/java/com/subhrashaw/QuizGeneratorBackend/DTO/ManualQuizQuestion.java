package com.subhrashaw.QuizGeneratorBackend.DTO;

import java.util.Arrays;

public class ManualQuizQuestion {
    private String question;
    private String options[];
    private String answer;
    private String type;
    private int mark;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    @Override
    public String toString() {
        return "ManualQuizQuestion{" +
                "question='" + question + '\'' +
                ", options=" + Arrays.toString(options) +
                ", answer='" + answer + '\'' +
                ", type='" + type + '\'' +
                ", mark=" + mark +
                '}';
    }
}

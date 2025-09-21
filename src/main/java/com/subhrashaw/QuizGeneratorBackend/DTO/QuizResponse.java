package com.subhrashaw.QuizGeneratorBackend.DTO;

public class QuizResponse {
    private int qid;
    private String question;
    private String option[];
    private String type;
    private String Marks;
    private String Negmark;
    private String answer;

    public QuizResponse(int qid, String question, String[] option, String type, String marks, String negmark, String answer) {
        this.qid = qid;
        this.question = question;
        this.option = option;
        this.type = type;
        Marks = marks;
        Negmark = negmark;
        this.answer = answer;
    }

    public int getQid() {
        return qid;
    }

    public void setQid(int qid) {
        this.qid = qid;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String[] getOption() {
        return option;
    }

    public void setOption(String[] option) {
        this.option = option;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMarks() {
        return Marks;
    }

    public void setMarks(String marks) {
        Marks = marks;
    }

    public String getNegmark() {
        return Negmark;
    }

    public void setNegmark(String negmark) {
        Negmark = negmark;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}

package com.subhrashaw.QuizGeneratorBackend.DTO;

public class EditQuestionFormat {
    private int questionsId;
    private String question;
    private String options[];
    private int marks;
    private int negMark;
    private String type;
    private String answer;

    public int getQuestionsId() {
        return questionsId;
    }

    public void setQuestionsId(int questionsId) {
        this.questionsId = questionsId;
    }

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

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    public int getNegMark() {
        return negMark;
    }

    public void setNegMark(int negMark) {
        this.negMark = negMark;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}

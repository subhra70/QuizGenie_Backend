package com.subhrashaw.QuizGeneratorBackend.Model;

import jakarta.persistence.*;

@Entity
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(columnDefinition = "TEXT")
    private String question;
    @Column(length = 1000)
    private String option1;
    @Column(length = 1000)
    private String option2;
    @Column(length = 1000)
    private String option3;
    @Column(length = 1000)
    private String option4;
    @Column(columnDefinition = "TEXT")
    private String answer;
    @ManyToOne
    @JoinColumn
    private QuizMarks marks;

    public QuizQuestion(String question, String option1, String option2, String option3, String option4, String answer, QuizMarks marks) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.answer = answer;
        this.marks = marks;
    }
    public QuizQuestion(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public QuizMarks getMarks() {
        return marks;
    }

    public void setMarks(QuizMarks marks) {
        this.marks = marks;
    }

    @Override
    public String toString() {
        return "QuizQuestion{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", option1='" + option1 + '\'' +
                ", option2='" + option2 + '\'' +
                ", option3='" + option3 + '\'' +
                ", option4='" + option4 + '\'' +
                ", answer='" + answer + '\'' +
                ", marks=" + marks +
                '}';
    }
}

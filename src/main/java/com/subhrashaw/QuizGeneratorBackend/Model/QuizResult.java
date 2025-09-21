package com.subhrashaw.QuizGeneratorBackend.Model;

import jakarta.persistence.*;


@Entity
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    @OneToOne
    @JoinColumn
    private QuizClass quizClass;
    private double obtainedMark;
    private String date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public QuizClass getQuizClass() {
        return quizClass;
    }

    public void setQuizClass(QuizClass quizClass) {
        this.quizClass = quizClass;
    }

    public double getObtainedMark() {
        return obtainedMark;
    }

    public void setObtainedMark(double obtainedMark) {
        this.obtainedMark = obtainedMark;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

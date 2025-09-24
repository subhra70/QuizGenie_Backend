package com.subhrashaw.QuizGeneratorBackend.Model;

import jakarta.persistence.*;


@Entity
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn
    private QuizClass quizClass;
    @ManyToOne
    @JoinColumn
    private QuizUsers quizUser;
    private boolean isPerformed;
    private double obtainedMark;
    private String date;
    private String role;

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

    public QuizUsers getQuizUser() {
        return quizUser;
    }

    public void setQuizUser(QuizUsers quizUser) {
        this.quizUser = quizUser;
    }

    public boolean isPerformed() {
        return isPerformed;
    }

    public void setPerformed(boolean performed) {
        isPerformed = performed;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

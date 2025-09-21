package com.subhrashaw.QuizGeneratorBackend.Model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class QuizClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    @OneToMany
    @JoinColumn
    private List<QuizQuestion> quizQuestion;
    private int duration;
    private String password;
    private boolean isPerformed;
    private boolean isLocked;
    private int fullMarks;

    public QuizClass(int id) {
        this.id = id;
    }

    public QuizClass() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<QuizQuestion> getQuizQuestion() {
        return quizQuestion;
    }

    public void setQuizQuestion(List<QuizQuestion> quizQuestion) {
        this.quizQuestion = quizQuestion;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String gmail) {
        this.email = gmail;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getFullMarks() {
        return fullMarks;
    }

    public void setFullMarks(int fullMarks) {
        this.fullMarks = fullMarks;
    }

    public boolean isPerformed() {
        return isPerformed;
    }

    public void setPerformed(boolean performed) {
        isPerformed = performed;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    @Override
    public String toString() {
        return "QuizClass{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", quizQuestion=" + quizQuestion +
                ", duration=" + duration +
                ", password='" + password + '\'' +
                ", isPerformed=" + isPerformed +
                ", isLocked=" + isLocked +
                ", fullMarks=" + fullMarks +
                '}';
    }
}

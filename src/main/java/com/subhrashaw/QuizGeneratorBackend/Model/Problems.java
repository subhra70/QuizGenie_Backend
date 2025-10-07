package com.subhrashaw.QuizGeneratorBackend.Model;

import jakarta.persistence.*;

@Entity
public class Problems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn
    private QuizUsers user;
    @Column(columnDefinition = "TEXT")
    private String problem;
    private String raisedDate;
    private String solveDate;
    private boolean solveStatus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public QuizUsers getUser() {
        return user;
    }

    public void setUser(QuizUsers user) {
        this.user = user;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getRaisedDate() {
        return raisedDate;
    }

    public void setRaisedDate(String raisedDate) {
        this.raisedDate = raisedDate;
    }

    public String getSolveDate() {
        return solveDate;
    }

    public void setSolveDate(String solveDate) {
        this.solveDate = solveDate;
    }

    public boolean isSolveStatus() {
        return solveStatus;
    }

    public void setSolveStatus(boolean solveStatus) {
        this.solveStatus = solveStatus;
    }
}

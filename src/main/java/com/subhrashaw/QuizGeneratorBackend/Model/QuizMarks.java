package com.subhrashaw.QuizGeneratorBackend.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class QuizMarks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String type;
    private int mark;
    private double negMark;
    public QuizMarks()
    {

    }

    public QuizMarks(String type, int mark, double negMark) {
        this.type = type;
        this.mark = mark;
        this.negMark = negMark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public double getNegMark() {
        return negMark;
    }

    public void setNegMark(double negMark) {
        this.negMark = negMark;
    }

    @Override
    public String toString() {
        return "QuizMarks{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", mark=" + mark +
                ", negMark=" + negMark +
                '}';
    }
}

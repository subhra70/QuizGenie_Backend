package com.subhrashaw.QuizGeneratorBackend.Model;

import jakarta.persistence.*;

@Entity
public class TrialTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne
    @JoinColumn
    private QuizUsers user;
    private int monthDuration;
    private int freeTrialAutogen;
    private int createTrial;
    private boolean isPremium;
    private String purchasedDate;
    private double amount;
    private boolean status;

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

    public int getMonthDuration() {
        return monthDuration;
    }

    public void setMonthDuration(int monthDuration) {
        this.monthDuration = monthDuration;
    }

    public int getFreeTrialAutogen() {
        return freeTrialAutogen;
    }

    public void setFreeTrialAutogen(int freeTrialAutogen) {
        this.freeTrialAutogen = freeTrialAutogen;
    }

    public String getPurchasedDate() {
        return purchasedDate;
    }

    public void setPurchasedDate(String purchasedDate) {
        this.purchasedDate = purchasedDate;
    }

    public int getCreateTrial() {
        return createTrial;
    }

    public void setCreateTrial(int createTrial) {
        this.createTrial = createTrial;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TrialTrack{" +
                "id=" + id +
                ", user=" + user +
                ", monthDuration=" + monthDuration +
                ", freeTrialAutogen=" + freeTrialAutogen +
                ", createTrial=" + createTrial +
                ", isPremium=" + isPremium +
                ", purchasedDate='" + purchasedDate + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}

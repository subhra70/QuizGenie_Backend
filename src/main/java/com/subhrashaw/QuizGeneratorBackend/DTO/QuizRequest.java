package com.subhrashaw.QuizGeneratorBackend.DTO;

public class QuizRequest {
    private String format;
    private int totalQuestion;
    private int fullMarks;
    private boolean negativeMark;
    private int mcq1;
    private int mcq2;
    private int msq1;
    private int msq2;
    private int nat1;
    private int nat2;
    private int duration;
    private String description;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getTotalQuestion() {
        return totalQuestion;
    }

    public void setTotalQuestion(int totalQuestion) {
        this.totalQuestion = totalQuestion;
    }

    public boolean isNegativeMark() {
        return negativeMark;
    }

    public void setNegativeMark(boolean negativeMark) {
        this.negativeMark = negativeMark;
    }

    public int getMcq1() {
        return mcq1;
    }

    public void setMcq1(int mcq1) {
        this.mcq1 = mcq1;
    }

    public int getMcq2() {
        return mcq2;
    }

    public void setMcq2(int mcq2) {
        this.mcq2 = mcq2;
    }

    public int getMsq1() {
        return msq1;
    }

    public void setMsq1(int msq1) {
        this.msq1 = msq1;
    }

    public int getMsq2() {
        return msq2;
    }

    public void setMsq2(int msq2) {
        this.msq2 = msq2;
    }

    public int getNat1() {
        return nat1;
    }

    public void setNat1(int nat1) {
        this.nat1 = nat1;
    }

    public int getNat2() {
        return nat2;
    }

    public void setNat2(int nat2) {
        this.nat2 = nat2;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFullMarks() {
        return fullMarks;
    }

    public void setFullMarks(int fullMarks) {
        this.fullMarks = fullMarks;
    }


    @Override
    public String toString() {
        return "QuizRequest{" +
                "format='" + format + '\'' +
                ", totalQuestion=" + totalQuestion +
                ", negativeMark=" + negativeMark +
                ", mcq1=" + mcq1 +
                ", mcq2=" + mcq2 +
                ", msq1=" + msq1 +
                ", msq2=" + msq2 +
                ", nat1=" + nat1 +
                ", nat2=" + nat2 +
                ", duration=" + duration +
                ", description='" + description + '\'' +
                '}';
    }
}

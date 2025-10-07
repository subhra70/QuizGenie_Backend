package com.subhrashaw.QuizGeneratorBackend.DTO;

import jakarta.persistence.Column;

public class ProblemDTO {
    @Column(columnDefinition = "TEXT")
    private String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}

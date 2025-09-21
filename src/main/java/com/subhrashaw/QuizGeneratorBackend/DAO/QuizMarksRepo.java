package com.subhrashaw.QuizGeneratorBackend.DAO;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizMarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizMarksRepo extends JpaRepository<QuizMarks,Integer> {

    QuizMarks findByTypeAndMarkAndNegMark(String type, int mark, double negMark);
}

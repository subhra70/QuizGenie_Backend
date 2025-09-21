package com.subhrashaw.QuizGeneratorBackend.DAO;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizMarks;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionsRepo extends JpaRepository<QuizQuestion,Integer> {
}

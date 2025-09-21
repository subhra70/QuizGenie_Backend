package com.subhrashaw.QuizGeneratorBackend.DAO;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizClass;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepo extends JpaRepository<QuizResult,Integer> {
    List<QuizResult> findAllByEmail(String email);

    QuizResult findByQuizClass(QuizClass quizClass);
}

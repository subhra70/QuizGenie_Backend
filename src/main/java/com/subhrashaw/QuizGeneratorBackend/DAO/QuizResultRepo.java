package com.subhrashaw.QuizGeneratorBackend.DAO;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizClass;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizResult;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepo extends JpaRepository<QuizResult,Integer> {
    QuizResult findByQuizClass(QuizClass quizClass);

    List<QuizResult> findAllByQuizUser(QuizUsers user);
}

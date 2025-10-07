package com.subhrashaw.QuizGeneratorBackend.DAO;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizClass;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizResult;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultRepo extends JpaRepository<QuizResult,Integer> {

    List<QuizResult> findAllByQuizUser(QuizUsers user);


    QuizResult findByQuizClassAndQuizUser(QuizClass quizClass, QuizUsers user);

    @Query("SELECT qr FROM QuizResult qr WHERE qr.quizClass = :quizClass ORDER BY qr.obtainedMark DESC")
    List<QuizResult> findByQuizClassSorted(@Param("quizClass") QuizClass quizClass);

}

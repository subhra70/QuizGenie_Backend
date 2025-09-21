package com.subhrashaw.QuizGeneratorBackend.DAO;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepo extends JpaRepository<QuizClass,Integer> {
}

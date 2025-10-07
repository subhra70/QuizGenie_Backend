package com.subhrashaw.QuizGeneratorBackend.DAO;

import com.subhrashaw.QuizGeneratorBackend.Model.Problems;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepo extends JpaRepository<Problems,Integer> {
}

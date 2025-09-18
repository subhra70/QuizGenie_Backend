package com.subhrashaw.QuizGeneratorBackend.DAO;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<QuizUsers,Integer> {
    QuizUsers findByEmail(String email);
}

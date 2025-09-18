package com.subhrashaw.QuizGeneratorBackend.Service;

import com.subhrashaw.QuizGeneratorBackend.DAO.UserRepo;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepo repo;

    public QuizUsers getUser(String email)
    {
        return repo.findByEmail(email);
    }
}

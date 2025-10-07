package com.subhrashaw.QuizGeneratorBackend.Service;

import com.subhrashaw.QuizGeneratorBackend.DAO.QuizClassRepo;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizClass;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizDetails;
import com.sun.security.auth.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class QuizDetailedService implements UserDetailsService {
    @Autowired
    private QuizClassRepo quizClassRepo;

    public UserDetails loadUserByUsername(String quizId) throws UsernameNotFoundException
    {
        QuizClass quizClass=null;
        int qid=Integer.parseInt(quizId);
        try
        {
            quizClass=quizClassRepo.findById(qid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(quizClass==null)
        {
            System.out.println("Quiz Not Found");
            throw new UsernameNotFoundException("404 not found");
        }
        System.out.println("Returning quiz principle");
        return new QuizDetails(quizClass);
    }
}

package com.subhrashaw.QuizGeneratorBackend.Service;

import com.subhrashaw.QuizGeneratorBackend.DAO.UserRepo;
import com.subhrashaw.QuizGeneratorBackend.DAO.UserTrackRepo;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import com.subhrashaw.QuizGeneratorBackend.Model.TrialTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepo repo;
    @Autowired
    private UserTrackRepo userTrackRepo;

    public QuizUsers getUser(String email)
    {
        return repo.findByEmail(email);
    }

    public TrialTrack getUserDetails(QuizUsers users) {
        return userTrackRepo.findByUser(users);
    }
    public void handleDetails(QuizUsers user)
    {
        TrialTrack userDetails=userTrackRepo.findByUser(user);
        userDetails.setFreeTrialAutogen(userDetails.getFreeTrialAutogen()-1);
        userTrackRepo.save(userDetails);
    }

    public List<TrialTrack> getAllUsers() {
        return userTrackRepo.findAll();
    }
}

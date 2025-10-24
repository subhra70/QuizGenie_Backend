package com.subhrashaw.QuizGeneratorBackend.Service;

import com.subhrashaw.QuizGeneratorBackend.DAO.UserRepo;
import com.subhrashaw.QuizGeneratorBackend.DAO.UserTrackRepo;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import com.subhrashaw.QuizGeneratorBackend.Model.TrialTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        TrialTrack res= userTrackRepo.findByUser(users);
        String storedDate=res.getPurchasedDate();
        LocalDate currDate=LocalDate.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date=LocalDate.parse(storedDate,formatter);
        LocalDate validDate=date.plusDays(res.getMonthDuration()*30);
        if(currDate.isAfter(validDate))
        {
            res.setAmount(0);
            res.setMonthDuration(0);
            res.setPremium(false);
            res.setFreeTrialAutogen(0);
            res.setCreateTrial(0);
            userTrackRepo.save(res);
        }
        return res;
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

package com.subhrashaw.QuizGeneratorBackend.DAO;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import com.subhrashaw.QuizGeneratorBackend.Model.TrialTrack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTrackRepo extends JpaRepository<TrialTrack,Integer> {
    TrialTrack findByUser(QuizUsers users);
}

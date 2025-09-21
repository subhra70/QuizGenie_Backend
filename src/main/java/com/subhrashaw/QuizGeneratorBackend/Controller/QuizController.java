package com.subhrashaw.QuizGeneratorBackend.Controller;

import com.subhrashaw.QuizGeneratorBackend.DTO.QuizRequest;
import com.subhrashaw.QuizGeneratorBackend.DTO.QuizResponse;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizClass;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizQuestion;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizResult;
import com.subhrashaw.QuizGeneratorBackend.Service.GenerationService;
import com.subhrashaw.QuizGeneratorBackend.Service.JwtService;
import com.subhrashaw.QuizGeneratorBackend.Service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class QuizController {
    @Autowired
    private QuizService quizService;
    @Autowired
    private GenerationService generationService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/generateWithFormat")
    public ResponseEntity<HttpStatus> generate(@RequestHeader("Authorization") String auth, @RequestBody QuizRequest request) {
        System.out.println("Invoked");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token = auth.substring(7);
        String email = jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        quizService.setMarks();
        List<QuizQuestion> list = generationService.generate(request);
        if (list == null || list.size() == 0) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        quizService.saveQuestions(email, list, request.getDuration(), request.getFullMarks());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/questionSet/{qid}")
    public ResponseEntity<?> getQuestionSet(@RequestHeader("Authorization") String auth, @PathVariable("qid") int id) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token = auth.substring(7);
        String email = jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        QuizClass questionSet = quizService.getQuestionSet(id, email);
        if (questionSet == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(questionSet, HttpStatus.OK);
    }

    @PostMapping("/answeredQuiz/{uid}")
    public ResponseEntity<HttpStatus> submitQuiz(@RequestHeader("Authorization") String auth, @RequestBody QuizResponse response, @PathVariable("uid") int uid) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token = auth.substring(7);
        String email = jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (uid == -1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        boolean calculateResult = quizService.calResult(response, uid,email);
        if (calculateResult) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/history")
    private ResponseEntity<?> getResultHistory(@RequestHeader("Authorization") String auth)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<QuizResult> resultSet=quizService.getResult(email);
        if(resultSet==null || resultSet.size()==0)
        {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(resultSet,HttpStatus.OK);
    }
}

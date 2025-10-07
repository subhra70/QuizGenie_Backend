package com.subhrashaw.QuizGeneratorBackend.Controller;

import com.subhrashaw.QuizGeneratorBackend.DTO.*;
import com.subhrashaw.QuizGeneratorBackend.Model.*;
import com.subhrashaw.QuizGeneratorBackend.Service.GenerationService;
import com.subhrashaw.QuizGeneratorBackend.Service.JwtService;
import com.subhrashaw.QuizGeneratorBackend.Service.QuizService;
import com.subhrashaw.QuizGeneratorBackend.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class QuizController {
    @Autowired
    private QuizService quizService;
    @Autowired
    private UserService userService;
    @Autowired
    private GenerationService generationService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private AuthenticationManager manager;

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
        GenerateResponse res=generationService.generate(request);
        if(res==null)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        List<QuizQuestion> list = res.getQuestions();
        int fullMarks=res.getFullMarks();
        if (list == null || list.size() == 0) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        quizService.saveQuestions(email, list, request.getDuration(), fullMarks,request.isNegativeMark());
        QuizUsers user= userService.getUser(email);
        userService.handleDetails(user);
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
    public ResponseEntity<HttpStatus> submitQuiz(@RequestHeader("Authorization") String auth, @RequestBody List<QuizResponse> response, @PathVariable("uid") int uid) {
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

    @PostMapping("/manualCreation")
    public ResponseEntity<HttpStatus> saveData(@RequestHeader("Authorization") String auth, @RequestBody ManualQuizRequest request)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        QuizRequest details= request.getQuizDetails();
        List<ManualQuizQuestion> questions=request.getQuestionDetails();
        System.out.println(details);
        System.out.println(questions.get(0).toString());
        if(details==null || questions==null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        boolean status=quizService.saveQuiz(details,questions,email);
        if(!status)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/handleUnlock/{qid}")
    public ResponseEntity<HttpStatus> Unlock(@RequestHeader("Authorization") String auth,@RequestBody String password,@PathVariable("qid")int qid)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            String quizId=Integer.toString(qid);
            Authentication authentication= manager.authenticate(new UsernamePasswordAuthenticationToken(quizId,password));
            if (authentication.isAuthenticated())
            {
                QuizClass quizClass=quizService.getQuizClass(qid);
                if(quizClass==null)
                {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                quizClass.setLocked(false);
                quizClass.setPassword(null);
                quizService.saveQuizClass(quizClass);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch (AuthenticationException e)
        {
            System.out.println("Authentication failed");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/handleLock/{qid}")
    public ResponseEntity<HttpStatus> Lock(@RequestHeader("Authorization") String auth, @RequestBody String password,@PathVariable("qid")int qid)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        QuizClass quizClass=quizService.getQuizClass(qid);
        if(quizClass==null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        quizClass.setLocked(true);
        quizClass.setPassword(encoder.encode(password));
        quizService.saveQuizClass(quizClass);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/passwordAuthentication/{quizId}")
    public ResponseEntity<HttpStatus> authenticate(@RequestHeader("Authorization") String auth,@RequestBody String password,@PathVariable("quizId") int id)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            String quizId=Integer.toString(id);
            Authentication authentication= manager.authenticate(new UsernamePasswordAuthenticationToken(quizId,password));
            if (authentication.isAuthenticated())
            {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch (AuthenticationException e)
        {
            System.out.println("Authentication failed");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/quiz/{id}")
    public ResponseEntity<HttpStatus> deleteQuiz(@RequestHeader("Authorization") String auth,@PathVariable("id") int id)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        quizService.deleteResult(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/quiz/{qid}")
    public ResponseEntity<QuizClass> findQuiz(@RequestHeader("Authorization") String auth,@PathVariable("qid") int id)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        QuizClass quiz=quizService.getQuizClass(id);
        if(quiz==null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(quiz,HttpStatus.OK);
    }

    @PostMapping("addToHistory")
    public ResponseEntity<HttpStatus> addQuizToHistory(@RequestHeader("Authorization") String auth,@RequestBody Map<String, Integer> body)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        int id=body.get("id");
        QuizUsers user= userService.getUser(email);
        TrialTrack userDetails= userService.getUserDetails(user);
        if(!userDetails.isPremium())
        {
            return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
        }
        QuizClass quizClass=quizService.getQuizClass(id);
        boolean saveStatus=quizService.saveToHistory(user,quizClass);
        if(!saveStatus)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/quiz")
    public ResponseEntity<HttpStatus> updateQuiz(@RequestHeader("Authorization") String auth, @RequestBody EditRequestFormat requestFormat)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        QuizClass quizClass=quizService.getQuizClass(requestFormat.getQuizId());
        if(quizClass==null)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        boolean updateStatus=quizService.updateQuizClass(quizClass,requestFormat);
        if(!updateStatus)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/result/{quizId}")
    public ResponseEntity<List<QuizResult>> resultLeadboard(@RequestHeader("Authorization") String auth,@PathVariable("quizId") int id)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        QuizClass quizClass=quizService.getQuizClass(id);
        if(quizClass==null)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        List<QuizResult> resultHistory=quizService.getAllResultByQuiz(quizClass);
        return new ResponseEntity<>(resultHistory,HttpStatus.OK);
    }
    @PostMapping("/problem")
    public ResponseEntity<HttpStatus> raiseQuery(@RequestHeader("Authorization") String auth,@RequestBody ProblemDTO raisedQuery)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        QuizUsers users= userService.getUser(email);
        if(users==null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        quizService.submitQuery(users,raisedQuery);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PutMapping("/problem/{id}")
    public ResponseEntity<List<Problems>> solveQuery(@RequestHeader("Authorization") String auth,@PathVariable("id") int id)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean status=quizService.solveQuery(id);
        if(!status)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        List<Problems> problemsList=quizService.getALLProblems();
        if(problemsList==null || problemsList.size()==0)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(problemsList,HttpStatus.OK);
    }
    @GetMapping("/problems")
    public ResponseEntity<List<Problems>> fetchProblems(@RequestHeader("Authorization") String auth)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Problems> problemsList=quizService.getALLProblems();
        if(problemsList==null || problemsList.size()==0)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(problemsList,HttpStatus.OK);
    }

    @DeleteMapping("/problem/{id}")
    public ResponseEntity<List<Problems>> deleteProblems(@RequestHeader("Authorization") String auth,@PathVariable("id") int id)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean status=quizService.deleteProblem(id);
        if(!status)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        List<Problems> problemsList=quizService.getALLProblems();
        if(problemsList==null || problemsList.size()==0)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(problemsList,HttpStatus.OK);
    }
    @PostMapping("/purchase/{id}")
    public ResponseEntity<HttpStatus> purchasePackage(@RequestHeader("Authorization") String auth,@PathVariable("id") int id)
    {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        System.out.println(id);
        boolean status=quizService.handlePurchase(email,id);
        if(!status)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

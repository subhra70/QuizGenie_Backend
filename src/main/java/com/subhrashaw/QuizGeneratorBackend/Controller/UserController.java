package com.subhrashaw.QuizGeneratorBackend.Controller;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import com.subhrashaw.QuizGeneratorBackend.Model.TrialTrack;
import com.subhrashaw.QuizGeneratorBackend.Service.JwtService;
import com.subhrashaw.QuizGeneratorBackend.Service.UserService;
import org.hibernate.annotations.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @GetMapping("user")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String header)
    {
        if(header==null || !header.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=header.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        QuizUsers user=userService.getUser(email);
        if(user==null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user,HttpStatus.OK);
    }
    @GetMapping("userDetails")
    public ResponseEntity<?> fetchDetails(@RequestHeader("Authorization") String auth)
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
        TrialTrack userDetails=userService.getUserDetails(users);
        if(userDetails==null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(userDetails,HttpStatus.OK);
    }
    @GetMapping("/allUsers")
    public ResponseEntity<List<TrialTrack>> fetchAllUsers(@RequestHeader("Authorization") String auth)
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
        if(!email.equals("shawsubhra68@gmail.com"))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<TrialTrack> allUsers=userService.getAllUsers();
        if(allUsers==null || allUsers.size()==0)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allUsers,HttpStatus.OK);
    }
}

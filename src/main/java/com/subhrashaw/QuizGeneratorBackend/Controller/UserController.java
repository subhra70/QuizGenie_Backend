package com.subhrashaw.QuizGeneratorBackend.Controller;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import com.subhrashaw.QuizGeneratorBackend.Service.JwtService;
import com.subhrashaw.QuizGeneratorBackend.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

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
        QuizUsers user=userService.getUser(email);
        if(user==null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user,HttpStatus.OK);
    }
}

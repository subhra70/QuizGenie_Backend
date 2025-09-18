package com.subhrashaw.QuizGeneratorBackend.Controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {
    @GetMapping("/hello")
    public String sayHello()
    {
        return "Hello Subhra";
    }
}

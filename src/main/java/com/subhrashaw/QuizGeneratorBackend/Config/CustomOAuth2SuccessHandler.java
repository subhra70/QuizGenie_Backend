package com.subhrashaw.QuizGeneratorBackend.Config;

import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import com.subhrashaw.QuizGeneratorBackend.Service.JwtService;
import com.subhrashaw.QuizGeneratorBackend.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;
    @Value("${frontend.url}")
    private String frontendURL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, IOException {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        String email = user.getAttribute("email");
        System.out.println("Invoked handler");
        String jwt = jwtService.generateToken(email);
        QuizUsers user1=userService.getUser(email);
        System.out.println(user1.getPicture());
        String image= URLEncoder.encode(user1.getPicture(), StandardCharsets.UTF_8.toString());
        String redirectUrl = frontendURL+"/?token=" + jwt+"&image="+image;
        System.out.println("Done");
        response.sendRedirect(redirectUrl);
    }
}


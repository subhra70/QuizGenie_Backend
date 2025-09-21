package com.subhrashaw.QuizGeneratorBackend.Config;

import com.subhrashaw.QuizGeneratorBackend.DAO.UserRepo;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizUsers;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Value("${frontend.url}")
    private String frontendURL;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(customAuthorizationRequestResolver())
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService())
                        )
                        .successHandler(customOAuth2SuccessHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> customOidcUserService() {
        OidcUserService delegate = new OidcUserService();
        System.out.println("Config invoked");
        return request -> {
            OidcUser oidcUser = delegate.loadUser(request);
            String email = oidcUser.getAttribute("email");
            String name = oidcUser.getAttribute("name");
            String picture=oidcUser.getPicture();

            // Save user if not already in DB (no password needed)
            if (email != null && userRepo.findByEmail(email) == null) {
                QuizUsers newUser = new QuizUsers();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setPicture(picture);
                userRepo.save(newUser);
                System.out.println("Google user saved to DB: " + email);
            } else {
                System.out.println("Google user already exists: " + email);
            }

            return oidcUser;
        };
    }

    @Bean
    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request);
                return customizeAuthorizationRequest(originalRequest);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request, clientRegistrationId);
                return customizeAuthorizationRequest(originalRequest);
            }

            private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest originalRequest) {
                if (originalRequest == null) return null;

                return OAuth2AuthorizationRequest.from(originalRequest)
                        .additionalParameters(params -> {
                            params.putAll(originalRequest.getAdditionalParameters());
                            params.put("prompt", "select_account");  // Force Gmail chooser
                        })
                        .build();
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendURL));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        System.out.println("CORS allowed origin: " + config.getAllowedOrigins());
        return source;
    }
}

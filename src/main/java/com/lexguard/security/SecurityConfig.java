package com.lexguard.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            .csrf(csrf -> csrf.disable())
            
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .authorizeHttpRequests(auth -> auth
                
                .requestMatchers(HttpMethod.GET, "/").permitAll()
                .requestMatchers(HttpMethod.GET, "/index.html", "/login.html", "/registro.html", 
                    "/dashboard.html", "/admin.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/registro").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/consultas/mis-consultas").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/consultas/crear").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/notificaciones/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/notificaciones/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )
            
            .httpBasic(basic -> basic
                .realmName("LexGuard API")
            )
            
            .exceptionHandling(exc -> exc
                .authenticationEntryPoint((request, response, authException) -> {
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Basic ")) {
                        response.setHeader("WWW-Authenticate", "Basic realm=\"LexGuard API\"");
                        response.setContentType("application/json");
                        response.setStatus(401);
                        response.getWriter().write("{\"error\":\"Credenciales inválidas\"}");
                    } else {
                        response.setContentType("application/json");
                        response.setStatus(401);
                        response.getWriter().write("{\"error\":\"No autenticado\"}");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json");
                    response.setStatus(403);
                    response.getWriter().write("{\"error\":\"Acceso denegado\"}");
                })
            );

        return http.build();
    }

    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        
        List<String> allowedOrigins = new java.util.ArrayList<>(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8080",
            "http://127.0.0.1:8080",
            "http://127.0.0.1:5173",
            "https://lexguard-ae3b9-67636.web.app",
            "https://lexguard-ae3b9.web.app",
            "https://lexguard-ae3b9.firebaseapp.com"
        ));

        configuration.setAllowedOrigins(allowedOrigins);

        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Accept-Language"
        ));

        
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Total-Count",
            "X-Page-Count"
        ));

        
        configuration.setAllowCredentials(true);

        
        configuration.setMaxAge(3600L); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

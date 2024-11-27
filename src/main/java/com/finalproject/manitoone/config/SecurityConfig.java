package com.finalproject.manitoone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration

public class SecurityConfig {

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
//                .antMatchers("/admin/"**).hasRole("ADMIN") // 어드민 페이지 생성 및 롤 생성 시 활성화
                .anyRequest().permitAll();
        return http.build();
    }
}

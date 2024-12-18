package com.finalproject.manitoone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(
            "http://localhost:8080",
            "http://ec2-43-201-222-153.ap-northeast-2.compute.amazonaws.com"
        )
        .allowedMethods("GET", "POST","PUT","DELETE")
        .allowCredentials(true);
  }
}
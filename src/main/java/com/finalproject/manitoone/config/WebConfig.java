package com.finalproject.manitoone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/profile-images/**")
        .addResourceLocations("file:/home/profile/images/");
    registry.addResourceHandler("/**") // 모든 경로에 대한 리소스 처리
        .addResourceLocations("classpath:/static/"); // 정적 리소스 경로
  }
}

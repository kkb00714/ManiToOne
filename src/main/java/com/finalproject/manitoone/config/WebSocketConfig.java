package com.finalproject.manitoone.config;

import com.finalproject.manitoone.aop.AlarmHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final AlarmHandler alarmHandler;

  public WebSocketConfig(AlarmHandler alarmHandler) {
    this.alarmHandler = alarmHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(alarmHandler, "/ws-alarm")
        .setAllowedOrigins("*"); // 필요한 경우 허용된 Origin 설정
  }
}

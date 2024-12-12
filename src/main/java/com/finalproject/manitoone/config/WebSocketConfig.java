package com.finalproject.manitoone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // 클라이언트에서 구독할 prefix 설정
    config.enableSimpleBroker("/topic"); // 브로커 경로
    // 클라이언트에서 메시지 보낼 때의 prefix
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // WebSocket 연결을 위한 엔드포인트 설정
    registry.addEndpoint("/ws") // 엔드포인트 URL
        .setAllowedOriginPatterns("http://localhost:8080")
        .withSockJS(); // SockJS 사용
  }
}

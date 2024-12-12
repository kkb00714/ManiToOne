package com.finalproject.manitoone.aop;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class AlarmHandler extends TextWebSocketHandler {

  private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

  //클라이언트가 서버에 접속 성공시 호출
  @Override
  public void afterConnectionEstablished(WebSocketSession session) {

  }

  //소켓에 메세지를 보냈을 때 호출
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload(); // 클라이언트가 보낸 메시지 (예: 이메일 또는 고유 ID)

    // 사용자 ID와 세션 매핑
    userSessions.put(payload, session); // payload = 이메일 또는 사용자 ID
  }

  // 연결이 종료됐을 때 호출
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    // 세션 제거
    userSessions.values().removeIf(s -> s.equals(session));
  }

  public void sendNotification(String email, String message) throws IOException {
    // email에 해당하는 WebSocketSession 가져오기
    WebSocketSession session = userSessions.get(email);

    // 세션이 유효한 경우 메시지 전송
    if (session != null && session.isOpen()) {
      session.sendMessage(new TextMessage(message)); // 메시지 전송
    }
  }
}

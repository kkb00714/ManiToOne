package com.finalproject.manitoone.controller.api;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionApiController {

  @GetMapping("/session-info")
  public ResponseEntity<Map<String, String>> getSessionInfo(HttpSession session) {
    Map<String, String> response = new HashMap<>();
    String nickname = (String) session.getAttribute("nickname");
    response.put("nickname", nickname);
    return ResponseEntity.ok(response);
  }
}

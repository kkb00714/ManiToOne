package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.service.AiFeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiFeedbackController {

  private final AiFeedbackService aiFeedbackService;

  @GetMapping("/ai-feedback")
  public ResponseEntity<Object> getAiFeedback(HttpServletRequest request) {
    return ResponseEntity.ok(aiFeedbackService.getAiFeedback(request.getSession()));
  }
}

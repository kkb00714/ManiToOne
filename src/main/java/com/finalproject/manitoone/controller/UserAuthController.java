package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.service.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserAuthController {
  private final UserAuthService userAuthService;

  @PostMapping("/signup")
  public ResponseEntity<String> signUp(
      @Valid
      @RequestBody UserSignUpDTO userSignUpDTO
      ) {
    String message = userAuthService.registerUser(userSignUpDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(message);
  }
}

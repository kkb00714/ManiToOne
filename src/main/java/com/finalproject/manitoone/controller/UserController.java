package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.dto.user.UserInformationResponseDto;
import com.finalproject.manitoone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

  private final UserService userService;

  @GetMapping("/{nickname}")
  public ResponseEntity<UserInformationResponseDto> getUserInformation(@PathVariable String nickname) {
    return ResponseEntity.ok(userService.getUserByNickname(nickname));
  }
}

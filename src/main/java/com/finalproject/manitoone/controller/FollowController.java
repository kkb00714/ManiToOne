package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.service.FollowService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follow")
public class FollowController {

  private final FollowService followService;

  @GetMapping("/{targetNickName}")
  public ResponseEntity<Void> followUnfollow(
      @PathVariable String targetNickName, HttpSession session) {
    if (Boolean.TRUE.equals(
        followService.toggleFollow((String) session.getAttribute("nickname"), targetNickName))) {
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }
}

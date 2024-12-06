package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.service.FollowService;
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

  @GetMapping("/{myNickName}/{targetNickName}")
  public ResponseEntity<Void> followUnfollow(@PathVariable String myNickName,
      @PathVariable String targetNickName) {
    // TODO: 내 유저의 ID를 추후 세션에서 받아오도록 변경 필요
    if (Boolean.TRUE.equals(followService.toggleFollow(myNickName, targetNickName))) {
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }
}

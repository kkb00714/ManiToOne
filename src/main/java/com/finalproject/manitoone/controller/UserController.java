package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.dto.user.UserInformationResponseDto;
import com.finalproject.manitoone.service.UserService;
import com.finalproject.manitoone.util.AlanUtil;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

  private final UserService userService;

  //TODO: 확인하셨다면 이 줄과 27번째 줄(System.out)을 지우고 커밋 부탁드립니다.
  @Async
  @GetMapping("/{nickname}")
  public CompletableFuture<ResponseEntity<UserInformationResponseDto>> getUserInformation(
      @PathVariable String nickname) {
    System.out.println(AlanUtil.getAlanAnswer("나 오늘 치킨 6마리 먹었다"));
    return CompletableFuture.supplyAsync(
        () -> ResponseEntity.ok(userService.getUserByNickname(nickname)));
  }

}

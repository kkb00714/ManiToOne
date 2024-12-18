package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.dto.user.UserInformationResponseDto;
import com.finalproject.manitoone.service.UserService;
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

  @Async
  @GetMapping("/{nickname}")
  public CompletableFuture<ResponseEntity<UserInformationResponseDto>> getUserInformation(
      @PathVariable String nickname) {
    return CompletableFuture.supplyAsync(
        () -> ResponseEntity.ok(userService.getUserByNickname(nickname)));
  }

}

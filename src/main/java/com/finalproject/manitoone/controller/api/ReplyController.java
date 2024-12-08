package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.AddReplyRequestDto;
import com.finalproject.manitoone.domain.dto.ReplyResponseDto;
import com.finalproject.manitoone.service.ReplyService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reply")
public class ReplyController {

  private final ReplyService replyService;

  // 답글 생성
  @PostMapping("/{postId}")
  public ResponseEntity<ReplyResponseDto> createReply(@PathVariable("postId") Long postId,
      @RequestBody AddReplyRequestDto request,
      HttpSession session) {
    User user = (User) session.getAttribute("user");
    ReplyResponseDto reply = replyService.createReply(postId, request, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(reply);
  }
}

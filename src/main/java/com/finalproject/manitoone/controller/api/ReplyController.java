package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.AddReplyRequestDto;
import com.finalproject.manitoone.domain.dto.AddReportRequestDto;
import com.finalproject.manitoone.domain.dto.ReplyResponseDto;
import com.finalproject.manitoone.domain.dto.ReportResponseDto;
import com.finalproject.manitoone.domain.dto.UpdateReplyRequestDto;
import com.finalproject.manitoone.service.ReplyService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReplyController {

  private final ReplyService replyService;

  // 답글 생성
  @PostMapping("/reply/{postId}")
  public ResponseEntity<ReplyResponseDto> createReply(@PathVariable("postId") Long postId,
      @RequestBody AddReplyRequestDto request,
      HttpSession session) {
    User user = (User) session.getAttribute("user");
    ReplyResponseDto reply = replyService.createReply(postId, request, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(reply);
  }

  // 답글의 답글 생성
  @PostMapping("/rereply/{replyPostId}")
  public ResponseEntity<ReplyResponseDto> createReReply(
      @PathVariable("replyPostId") Long replyPostId,
      @RequestBody AddReplyRequestDto request,
      HttpSession session) {
    User user = (User) session.getAttribute("user");
    ReplyResponseDto rereply = replyService.createReReply(replyPostId, request, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(rereply);
  }

  // 답글 수정
  @PutMapping("/reply/{replyId}")
  public ResponseEntity<ReplyResponseDto> updateReply(@PathVariable("replyId") Long replyId,
      @RequestBody UpdateReplyRequestDto request,
      HttpSession session) {
    User user = (User) session.getAttribute("user");
    ReplyResponseDto updatedReply = replyService.updateReply(replyId, request, user);
    return ResponseEntity.ok(updatedReply);
  }

  // 답글 삭제
  @DeleteMapping("/reply/{replyId}")
  public ResponseEntity<Void> deleteReply(@PathVariable("replyId") Long replyId,
      HttpSession session) {
    User user = (User) session.getAttribute("user");
    replyService.deleteReply(replyId, user);
    return ResponseEntity.ok().build();
  }

  // 답글 신고
  @PutMapping("/reply/report/{replyId}")
  public ResponseEntity<ReportResponseDto> reportReply(@PathVariable("replyId") Long replyId,
      @RequestBody AddReportRequestDto request,
      HttpSession session) {
    User user = (User) session.getAttribute("user");
    ReportResponseDto report = replyService.reportReply(replyId, request, user);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(report);
  }

  // 답글 조회
  @GetMapping("/replies/{postId}")
  public ResponseEntity<Page<ReplyResponseDto>> getReplies(@PathVariable("postId") Long postId,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ReplyResponseDto> replies = replyService.getReplies(postId, pageable);
    return ResponseEntity.ok(replies);
  }

  // 답글의 답글 조회
  @GetMapping("/rereplies/{postId}")
  public ResponseEntity<List<ReplyResponseDto>> getReReplies(@PathVariable("postId") Long postId) {
    List<ReplyResponseDto> rereplies = replyService.getReReplies(postId);
    return ResponseEntity.ok(rereplies);
  }
}

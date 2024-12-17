package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.domain.dto.AddReplyRequestDto;
import com.finalproject.manitoone.domain.dto.AddReportRequestDto;
import com.finalproject.manitoone.domain.dto.ReplyResponseDto;
import com.finalproject.manitoone.domain.dto.ReportResponseDto;
import com.finalproject.manitoone.domain.dto.UpdateReplyRequestDto;
import com.finalproject.manitoone.service.ReplyService;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    String email = session.getAttribute("email") + "";

    ReplyResponseDto reply = replyService.createReply(postId, request, email);
    return ResponseEntity.status(HttpStatus.CREATED).body(reply);
  }

  // 답글의 답글 생성
  @PostMapping("/rereply/{replyId}")
  public ResponseEntity<ReplyResponseDto> createReReply(@PathVariable("replyId") Long replyId,
      @RequestBody AddReplyRequestDto request,
      HttpSession session) {
    String email = session.getAttribute("email") + "";

    ReplyResponseDto rereply = replyService.createReReply(replyId, request, email);
    return ResponseEntity.status(HttpStatus.CREATED).body(rereply);
  }

  // 답글 수정
  @PutMapping("/reply/{replyId}")
  public ResponseEntity<ReplyResponseDto> updateReply(@PathVariable("replyId") Long replyId,
      @RequestBody UpdateReplyRequestDto request,
      HttpSession session) {
    String email = session.getAttribute("email") + "";

    ReplyResponseDto updatedReply = replyService.updateReply(replyId, request, email);
    return ResponseEntity.ok(updatedReply);
  }

  // 답글 삭제
  @DeleteMapping("/reply/{replyId}")
  public ResponseEntity<Void> deleteReply(@PathVariable("replyId") Long replyId,
      HttpSession session) {
    String email = session.getAttribute("email") + "";

    replyService.deleteReply(replyId, email);
    return ResponseEntity.ok().build();
  }

  // 답글 신고
  @PostMapping("/reply/report/{replyId}")
  public ResponseEntity<ReportResponseDto> reportReply(@PathVariable("replyId") Long replyId,
      @RequestParam("reportType") String reportType,
      HttpSession session) {
    String email = session.getAttribute("email") + "";

    ReportResponseDto report = replyService.reportReply(replyId, reportType, email);
    return ResponseEntity.status(HttpStatus.CREATED).body(report);
  }

  // 답글 좋아요
  @PostMapping("/reply/like/{replyId}")
  public ResponseEntity<ReplyResponseDto> likeReply(@PathVariable("replyId") Long replyId,
      HttpSession session) {
    String email = session.getAttribute("email") + "";

    ReplyResponseDto reply = replyService.likeReply(replyId, email);
    return ResponseEntity.ok(reply);
  }

  // 답글 숨기기
  @PutMapping("/reply/hidden/{replyId}")
  public ResponseEntity<Void> hideReply(@PathVariable("replyId") Long replyId,
      HttpSession session) {
    String email = (String) session.getAttribute("email");
    replyService.hideReply(replyId, email);
    return ResponseEntity.ok().build();
  }

  // 게시글 답글 조회
  @GetMapping("/replies/{postId}")
  public ResponseEntity<Page<ReplyResponseDto>> getReplies(@PathVariable("postId") Long postId,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ReplyResponseDto> replies = replyService.getReplies(postId, pageable);
    return ResponseEntity.ok(replies);
  }

  // 답글 단건 조회
  @GetMapping("/reply/{replyId}")
  public ResponseEntity<ReplyResponseDto> getReply(@PathVariable("replyId") Long replyId) {
    ReplyResponseDto reply = replyService.getReply(replyId);
    return ResponseEntity.ok(reply);
  }

  // 답글 개수 조회
  @GetMapping("/replies/number/{postId}")
  public ResponseEntity<Integer> getRepliesNum(@PathVariable("postId") Long postId) {
    Integer num = replyService.getRepliesNum(postId);
    return ResponseEntity.ok(num);
  }

  // 답글의 답글 조회
  @GetMapping("/rereplies/{replyId}")
  public ResponseEntity<Page<ReplyResponseDto>> getReReplies(@PathVariable("replyId") Long replyId,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ReplyResponseDto> rereplies = replyService.getReReplies(replyId, pageable);
    return ResponseEntity.ok(rereplies);
  }

  // 답글의 답글 개수 조회
  @GetMapping("/rereplies/number/{replyId}")
  public ResponseEntity<Integer> getReRepliesNum(@PathVariable("replyId") Long replyId) {
    Integer num = replyService.getReRepliesNum(replyId);
    return ResponseEntity.ok(num);
  }

  // 답글 좋아요 개수 조회
  @GetMapping("/reply/like/number/{replyId}")
  public ResponseEntity<Integer> getReplyLikesNum(@PathVariable("replyId") Long replyId) {
    Integer num = replyService.getReplyLikesNum(replyId);
    return ResponseEntity.ok(num);
  }
}

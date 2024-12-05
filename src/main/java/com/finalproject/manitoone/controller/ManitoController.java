package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.constants.ManitoErrorMessages;
import com.finalproject.manitoone.dto.manito.ManitoAnswerRequestDto;
import com.finalproject.manitoone.dto.manito.ManitoLetterRequestDto;
import com.finalproject.manitoone.dto.manito.ManitoLetterResponseDto;
import com.finalproject.manitoone.dto.manito.ManitoPageResponseDto;
import com.finalproject.manitoone.service.ManitoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ManitoController {

  private final ManitoService manitoService;

  // 마니또 편지 생성
  @PostMapping("/manito/letter/{manitoPostId}")
  public ResponseEntity<ManitoLetterResponseDto> createManitoLetter(
      @PathVariable Long manitoPostId,
      @Valid @RequestBody ManitoLetterRequestDto request,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(manitoService.createLetter(manitoPostId, request, userDetails.getUsername()));
  }

  // 편지에 대한 답장
  @PutMapping("/manito/answer/{manitoPostId}")
  public ResponseEntity<ManitoLetterResponseDto> answerManitoLetter(
      @PathVariable Long manitoPostId,
      @Valid @RequestBody ManitoAnswerRequestDto request,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    return ResponseEntity.ok(
        manitoService.answerManitoLetter(manitoPostId, request.getAnswerComment(),
            userDetails.getUsername())
    );
  }

  // 편지 신고
  @PutMapping("/manito/report/{manitoPostId}")
  public ResponseEntity<Void> reportManitoLetter(
      @PathVariable Long manitoPostId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    manitoService.reportManitoLetter(manitoPostId, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  // 편지 공개 여부
  @PutMapping("/manito/hide/letter/{manitoPostId}")
  public ResponseEntity<Void> toggleManitoLetterVisibility(
      @PathVariable Long manitoPostId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    manitoService.toggleManitoLetterVisibility(manitoPostId, userDetails.getUsername());
    return ResponseEntity.ok().build();
  }

  // 받은 마니또 편지 + 페이징
  @GetMapping("/receivemanito/{nickname}")
  public ResponseEntity<ManitoPageResponseDto> getReceiveManito(
      @PathVariable String nickname,
      @AuthenticationPrincipal UserDetails userDetails,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    if (!userDetails.getUsername().equals(nickname)) {
      throw new AccessDeniedException(ManitoErrorMessages.NO_PERMISSION_MAILBOX.getMessage());
    }
    return ResponseEntity.ok(manitoService.getReceiveManito(nickname, pageable));
  }

  // 보낸 마니또 편지 + 페이징
  @GetMapping("/sendmanito/{nickname}")
  public ResponseEntity<ManitoPageResponseDto> getSendManito(
      @PathVariable String nickname,
      @AuthenticationPrincipal UserDetails userDetails,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    if (!userDetails.getUsername().equals(nickname)) {
      throw new AccessDeniedException(ManitoErrorMessages.NO_PERMISSION_MAILBOX.getMessage());
    }

    return ResponseEntity.ok(manitoService.getSendManito(nickname, pageable));
  }

  // 답장 신고
  @PutMapping("/manito/report/answer/{manitoPostId}")
  public ResponseEntity<Void> reportManitoAnswer(
      @PathVariable Long manitoPostId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    manitoService.reportManitoAnswer(manitoPostId, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

}

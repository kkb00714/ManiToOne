package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.constants.ManitoErrorMessages;
import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.dto.ReportRequestDto;
import com.finalproject.manitoone.domain.dto.admin.ReportStatusResponseDto;
import com.finalproject.manitoone.dto.manito.ManitoAnswerRequestDto;
import com.finalproject.manitoone.dto.manito.ManitoLetterRequestDto;
import com.finalproject.manitoone.dto.manito.ManitoLetterResponseDto;
import com.finalproject.manitoone.dto.manito.ManitoMatchResponseDto;
import com.finalproject.manitoone.dto.manito.ManitoPageResponseDto;
import com.finalproject.manitoone.service.ManitoMatchesService;
import com.finalproject.manitoone.service.ManitoService;
import com.finalproject.manitoone.service.ReportService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ManitoController {

  private final ManitoService manitoService;
  private final ReportService reportService;
  private final ManitoMatchesService manitoMatchesService;


  private String validateSession(HttpSession session) {
    String nickname = (String) session.getAttribute("nickname");
    if (nickname == null) {
      throw new AccessDeniedException("로그인이 필요합니다.");
    }
    return nickname;
  }

  private void validateUserAccess(String pathNickname, String sessionNickname) {
    if (!pathNickname.equals(sessionNickname)) {
      throw new AccessDeniedException(ManitoErrorMessages.NO_PERMISSION_MAILBOX.getMessage());
    }
  }

  // 마니또 편지 생성
  // manitoPostId -> manitoMatchesId로 변경
  @PostMapping("/manito/letter/{manitoMatchesId}")
  public ResponseEntity<ManitoLetterResponseDto> createManitoLetter(
      @PathVariable Long manitoMatchesId,
      @Valid @RequestBody ManitoLetterRequestDto request,
      HttpSession session
  ) {
    try {
      String nickname = validateSession(session);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(manitoService.createLetter(manitoMatchesId, request, nickname));
    } catch (EntityNotFoundException e) {
      log.error("Manito match not found: {}", manitoMatchesId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (Exception e) {
      log.error("Error creating manito letter: ", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "편지 작성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
  }

  // Pass 기능
  @PutMapping("/manito/pass/{manitoMatchesId}")
  public ResponseEntity<Void> passManitoMatch(
      @PathVariable Long manitoMatchesId,
      HttpSession session
  ) {
    String nickname = validateSession(session);
    manitoMatchesService.passMatch(manitoMatchesId, nickname);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/manito/match")
  public ResponseEntity<ManitoMatchResponseDto> createMatch(HttpSession session) {
    String nickname = validateSession(session);
    ManitoMatches match = manitoMatchesService.createMatch(nickname);
    return ResponseEntity.ok(ManitoMatchResponseDto.from(match));
  }


  // 편지에 대한 답장
  @PutMapping("/manito/answer/{manitoPostId}")
  public ResponseEntity<ManitoLetterResponseDto> answerManitoLetter(
      @PathVariable Long manitoPostId,
      @Valid @RequestBody ManitoAnswerRequestDto request,
      HttpSession session
  ) {
    String nickname = validateSession(session);
    return ResponseEntity.ok(
        manitoService.answerManitoLetter(manitoPostId, request.getAnswerComment(), nickname)
    );
  }

  // 편지 공개 여부
  @PutMapping("/manito/hide/letter/{manitoPostId}")
  public ResponseEntity<Void> toggleManitoLetterVisibility(
      @PathVariable Long manitoPostId,
      HttpSession session
  ) {
    String nickname = validateSession(session);
    manitoService.toggleManitoLetterVisibility(manitoPostId, nickname);
    return ResponseEntity.ok().build();
  }

  // 받은 마니또 편지 + 페이징
  @GetMapping("/receivemanito/{nickname}")
  public ResponseEntity<ManitoPageResponseDto> getReceiveManito(
      @PathVariable String nickname,
      HttpSession session,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    String sessionNickname = validateSession(session);
    validateUserAccess(nickname, sessionNickname);
    return ResponseEntity.ok(manitoService.getReceiveManito(nickname, pageable));
  }

  // 보낸 마니또 편지 + 페이징
  @GetMapping("/sendmanito/{nickname}")
  public ResponseEntity<ManitoPageResponseDto> getSendManito(
      @PathVariable String nickname,
      HttpSession session,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    String sessionNickname = validateSession(session);
    validateUserAccess(nickname, sessionNickname);
    return ResponseEntity.ok(manitoService.getSendManito(nickname, pageable));
  }

  // 마니또 편지 신고
  @PutMapping("/manito/report/{manitoLetterId}")
  public ResponseEntity<Void> reportManitoLetter(
      @PathVariable Long manitoLetterId,
      @Valid @RequestBody ReportRequestDto requestDto,
      HttpSession session
  ) {
    String nickname = validateSession(session);
    reportService.reportManitoLetter(manitoLetterId, nickname, requestDto);
    return ResponseEntity.ok().build();
  }

  // 마니또 편지의 답장 신고
  @PutMapping("/manito/report/answer/{manitoLetterId}")
  public ResponseEntity<Void> reportManitoAnswer(
      @PathVariable Long manitoLetterId,
      @Valid @RequestBody ReportRequestDto requestDto,
      HttpSession session
  ) {
    String nickname = validateSession(session);
    reportService.reportManitoAnswer(manitoLetterId, nickname, requestDto);
    return ResponseEntity.ok().build();
  }

  // 단일 편지 조회
  @GetMapping("/manito/letter/{letterId}")
  public ResponseEntity<ManitoLetterResponseDto> getManitoLetter(
      @PathVariable Long letterId,
      HttpSession session
  ) {
    String nickname = validateSession(session);

    ManitoLetterResponseDto letter = manitoService.getLetterWithPermissionCheck(letterId, nickname);
    return ResponseEntity.ok(letter);
  }


  // 신고 상태 조회
  @GetMapping("/manito/report/status/{letterId}")
  public ResponseEntity<ReportStatusResponseDto> checkReportStatus(
      @PathVariable Long letterId,
      @RequestParam ReportObjectType type,
      HttpSession session
  ) {
    validateSession(session);

    ReportStatusResponseDto status = reportService.checkReportStatus(type, letterId);
    return ResponseEntity.ok(status);
  }
}


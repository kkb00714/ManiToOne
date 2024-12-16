package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.ManitoErrorMessages;
import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.domain.ManitoLetter;
import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.dto.manito.ManitoLetterRequestDto;
import com.finalproject.manitoone.dto.manito.ManitoLetterResponseDto;
import com.finalproject.manitoone.dto.manito.ManitoPageResponseDto;
import com.finalproject.manitoone.repository.ManitoLetterRepository;
import com.finalproject.manitoone.repository.ManitoMatchesRepository;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.util.NotificationUtil;
import com.finalproject.manitoone.util.TimeFormatter;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ManitoService {

  private final ManitoLetterRepository manitoLetterRepository;
  private final ManitoMatchesRepository manitoMatchesRepository;
  private final UserRepository userRepository;

  private final NotificationUtil notificationUtil;


  // 마니또 매칭 검증 메서드
  private void validateManitoMatch(Long manitoMatchesId) {
    manitoMatchesRepository.findById(manitoMatchesId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_MATCH_NOT_FOUND.getMessage()));
  }

  @Transactional(readOnly = true)
  public ManitoLetterResponseDto getLetterByMatchIdAndNickname(Long manitoMatchesId,
      String nickname) {
    validateManitoMatch(manitoMatchesId);

    return manitoLetterRepository.findByManitoMatches_ManitoMatchesId(manitoMatchesId)
        .map(letter -> buildLetterResponseDto(letter, nickname))
        .orElse(null);
  }


  // 마니또 편지 생성
  @Transactional
  public ManitoLetterResponseDto createLetter(Long manitoMatchesId,
      ManitoLetterRequestDto requestDto, String userNickname) {
    ManitoMatches match = manitoMatchesRepository.findById(manitoMatchesId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_MATCH_NOT_FOUND.getMessage()));

    // 매칭 상태 확인
    match.validateMatchStatus();  // MATCHED 상태인지 확인

    User user = userRepository.findUserByNickname(userNickname)
        .orElseThrow(
            () -> new EntityNotFoundException(ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    validateLetterCreation(match, user);

    ManitoLetter manitoLetter = requestDto.toEntity(match);
    ManitoLetter savedLetter = manitoLetterRepository.save(manitoLetter);

    try {
      notificationUtil.createNotification(match.getMatchedPostId().getUser().getNickname(), user, NotiType.MANITO_COMMENT,
          manitoLetter.getManitoLetterId());
    } catch (IOException e) {
      log.error(e.getMessage());
    }

    return buildLetterResponseDto(savedLetter, userNickname);
  }


  // 편지 생성 유효성 검사
  private void validateLetterCreation(ManitoMatches match, User user) {
    // isManito 체크만 유지하고 isSelected 체크는 제거
    if (Boolean.FALSE.equals(match.getMatchedPostId().getIsManito())) {
      throw new IllegalStateException(ManitoErrorMessages.NOT_MANITO_POST.getMessage());
    }

    // 매칭된 유저가 맞는지 확인
    if (!match.getMatchedUserId().equals(user)) {
      throw new IllegalStateException(ManitoErrorMessages.NOT_MATCHED_USER.getMessage());
    }

    // 이미 편지를 보냈는지 확인
    if (manitoLetterRepository.findByManitoMatches_ManitoMatchesId(match.getManitoMatchesId())
        .isPresent()) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_REPLIED.getMessage());
    }

    // 자신의 게시물에 편지를 보내는지 확인
    if (match.getMatchedPostId().getUser().equals(user)) {
      throw new IllegalStateException(ManitoErrorMessages.OWN_POST_LETTER.getMessage());
    }
  }

  // ResponseDto 변환
  private ManitoLetterResponseDto buildLetterResponseDto(ManitoLetter letter,
      String currentUserNickname) {
    User currentUser = userRepository.findUserByNickname(currentUserNickname)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    return ManitoLetterResponseDto.builder()
        .manitoLetterId(letter.getManitoLetterId())
        .letterContent(letter.getLetterContent())
        .musicUrl(letter.getMusicUrl())
        .musicComment(letter.getMusicComment())
        .isPublic(letter.isPublic())
        .isReport(letter.isReport())
        .isAnswerReport(letter.isAnswerReport())
        .answerLetter(letter.getAnswerLetter())
        .timeDiff(TimeFormatter.formatTimeDiff(letter.getCreatedAt()))
        .isOwner(letter.isOwnedBy(currentUser))
        .createdAt(letter.getCreatedAt())
        .formattedCreatedAt(TimeFormatter.formatDateTime(letter.getCreatedAt()))
        .build();
  }

  // Page Response 변환
  private ManitoPageResponseDto buildPageResponseDto(Page<ManitoLetter> letters, String nickname) {
    return ManitoPageResponseDto.builder()
        .content(letters.getContent().stream()
            .map(letter -> buildLetterResponseDto(letter, nickname))
            .toList())
        .currentPage(letters.getNumber())
        .totalPages(letters.getTotalPages())
        .totalElements(letters.getTotalElements())
        .hasNext(letters.hasNext())
        .build();
  }

  // 받은 편지 조회
  public ManitoPageResponseDto getReceiveManito(String nickname, Pageable pageable) {
    Page<ManitoLetter> letters = manitoLetterRepository
        .findByManitoMatches_MatchedPostId_User_Nickname(nickname, pageable);

    return buildPageResponseDto(letters, nickname);
  }

  // 보낸 편지 조회
  public ManitoPageResponseDto getSendManito(String nickname, Pageable pageable) {
    Page<ManitoLetter> letters = manitoLetterRepository
        .findByManitoMatches_MatchedUserId_Nickname(nickname, pageable);

    return buildPageResponseDto(letters, nickname);
  }

  // 편지 공개 토글
  public void toggleManitoLetterVisibility(Long manitoLetterId, String userNickname) {
    ManitoLetter manitoLetter = manitoLetterRepository.findById(manitoLetterId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_LETTER_NOT_FOUND.getMessage()));

    manitoLetter.toggleVisibility(userNickname);
  }

  // 편지에 답장
  public ManitoLetterResponseDto answerManitoLetter(Long manitoLetterId, String answerLetter,
      String userNickname) {
    ManitoLetter manitoLetter = manitoLetterRepository.findById(manitoLetterId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_LETTER_NOT_FOUND.getMessage()));

    manitoLetter.addAnswer(answerLetter, userNickname);

    try {
      notificationUtil.createNotification(manitoLetter.getLetterWriter().getNickname(), manitoLetter.getLetterReceiver(), NotiType.MANITO_THANK_COMMENT,
          manitoLetter.getManitoLetterId());
    } catch (IOException e) {
      log.error(e.getMessage());
    }

    return buildLetterResponseDto(manitoLetter, userNickname);
  }

  // 단일 편지 조회
  @Transactional(readOnly = true)
  public ManitoLetterResponseDto getLetter(Long letterId) {
    ManitoLetter letter = manitoLetterRepository.findById(letterId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_LETTER_NOT_FOUND.getMessage()));

    return buildLetterResponseDto(letter, letter.getLetterReceiver().getNickname());
  }

  @Transactional(readOnly = true)
  public ManitoLetterResponseDto getLetterWithPermissionCheck(Long letterId, String nickname) {
    ManitoLetter letter = manitoLetterRepository.findById(letterId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_LETTER_NOT_FOUND.getMessage()));

    User currentUser = userRepository.findUserByNickname(nickname)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    boolean hasPermission = letter.getLetterReceiver().equals(currentUser) ||
        letter.getLetterWriter().equals(currentUser) ||
        letter.isPublic();

    if (!hasPermission) {
      throw new AccessDeniedException(ManitoErrorMessages.NO_PERMISSION_LETTER.getMessage());
    }

    return buildLetterResponseDto(letter, nickname);
  }

}

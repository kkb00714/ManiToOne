package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.ManitoErrorMessages;
import com.finalproject.manitoone.domain.ManitoLetter;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.dto.manito.ManitoLetterRequestDto;
import com.finalproject.manitoone.dto.manito.ManitoLetterResponseDto;
import com.finalproject.manitoone.dto.manito.ManitoPageResponseDto;
import com.finalproject.manitoone.repository.ManitoLetterRepository;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.util.TimeFormatter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ManitoService {

  private final ManitoLetterRepository manitoLetterRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  // 마니또 편지 생성
  public ManitoLetterResponseDto createLetter(Long postId, ManitoLetterRequestDto requestDto,
      String userNickname) {

    Post post = postRepository.findById(postId)
        .orElseThrow(
            () -> new EntityNotFoundException(ManitoErrorMessages.POST_NOT_FOUND.getMessage()));

    User user = userRepository.findUserByNickname(userNickname)
        .orElseThrow(
            () -> new EntityNotFoundException(ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    validateLetterCreation(post, user);

    ManitoLetter manitoLetter = requestDto.toEntity(post, user);
    ManitoLetter savedLetter = manitoLetterRepository.save(manitoLetter);

    return buildLetterResponseDto(savedLetter, userNickname);
  }

  // 편지 생성 유효성 검사
  private void validateLetterCreation(Post post, User user) {
    if (Boolean.FALSE.equals(post.getIsManito())) {
      throw new IllegalStateException(ManitoErrorMessages.NOT_MANITO_POST.getMessage());
    }

    if (Boolean.FALSE.equals(post.getIsSelected())) {
      throw new IllegalStateException(ManitoErrorMessages.NOT_SELECTED_POST.getMessage());
    }

    if (manitoLetterRepository.findByPostIdAndUser(post, user).isPresent()) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_REPLIED.getMessage());
    }

    if (post.getUser().equals(user)) {
      throw new IllegalStateException(ManitoErrorMessages.OWN_POST_LETTER.getMessage());
    }
  }

  // ResponseDto 변환
  private ManitoLetterResponseDto buildLetterResponseDto(ManitoLetter letter,
      String currentUserNickname) {
    User currentUser = userRepository.findUserByNickname(currentUserNickname)
        .orElseThrow(
            () -> new EntityNotFoundException(ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    return ManitoLetterResponseDto.builder()
        .manitoLetterId(letter.getManitoLetterId())
        .letterContent(letter.getLetterContent())
        .musicUrl(letter.getMusicUrl())
        .musicComment(letter.getMusicComment())
        .isPublic(letter.isPublic())
        .isReport(letter.isReport())
        .answerLetter(letter.getAnswerLetter())
        .timeDiff(TimeFormatter.formatTimeDiff(letter.getCreatedAt()))
        .isOwner(letter.isOwnedBy(currentUser))
        .build();
  }

  // 받은 편지 조회
  public ManitoPageResponseDto getReceiveManito(String nickname, Pageable pageable) {
    Page<ManitoLetter> letters = manitoLetterRepository.findByPostId_User_Nickname(nickname,
        pageable);

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

  // 보낸 편지 조회
  public ManitoPageResponseDto getSendManito(String nickname, Pageable pageable) {
    Page<ManitoLetter> letters = manitoLetterRepository.findByUser_Nickname(nickname, pageable);

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

  // 편지 공개 토글
  public void toggleManitoLetterVisibility(Long manitoLetterId, String userNickname) {
    ManitoLetter manitoLetter = manitoLetterRepository.findById(manitoLetterId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_LETTER_NOT_FOUND.getMessage()));

    if (!manitoLetter.getPost().getUser().getNickname().equals(userNickname)) {
      throw new IllegalStateException(ManitoErrorMessages.NO_PERMISSION_VISIBILITY.getMessage());
    }

    manitoLetter.toggleVisibility(userNickname);
  }

  // 편지 신고
  public void reportManitoLetter(Long manitoLetterId, String userNickname) {
    ManitoLetter manitoLetter = manitoLetterRepository.findById(manitoLetterId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_LETTER_NOT_FOUND.getMessage()));

    if (!manitoLetter.getPost().getUser().getNickname().equals(userNickname)) {
      throw new IllegalStateException(ManitoErrorMessages.NO_PERMISSION_REPORT.getMessage());
    }

    manitoLetter.reportLetter();
  }

  // 편지에 답장
  public ManitoLetterResponseDto answerManitoLetter(Long manitoLetterId, String answerLetter,
      String userNickname) {
    ManitoLetter manitoLetter = manitoLetterRepository.findById(manitoLetterId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_LETTER_NOT_FOUND.getMessage()));

    manitoLetter.addAnswer(answerLetter, userNickname);

    return buildLetterResponseDto(manitoLetter, userNickname);
  }

  // 답장 신고
  public void reportManitoAnswer(Long manitoLetterId, String userNickname) {
    ManitoLetter manitoLetter = manitoLetterRepository.findById(manitoLetterId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_LETTER_NOT_FOUND.getMessage()));

    manitoLetter.reportAnswer(userNickname);
  }
}

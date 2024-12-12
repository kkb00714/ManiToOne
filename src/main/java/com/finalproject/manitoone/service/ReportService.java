package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.ManitoErrorMessages;
import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.domain.ManitoLetter;
import com.finalproject.manitoone.domain.Report;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.ReportRequestDto;
import com.finalproject.manitoone.repository.ManitoLetterRepository;
import com.finalproject.manitoone.repository.ReportRepository;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {
  private final ReportRepository reportRepository;
  private final UserRepository userRepository;
  private final ManitoLetterRepository manitoLetterRepository;

  // 마니또 편지 신고
  public void reportManitoLetter(Long manitoLetterId, String userNickname, ReportRequestDto requestDto){
    User user = userRepository.findUserByNickname(userNickname)
        .orElseThrow(() -> new EntityNotFoundException(ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    ManitoLetter manitoLetter = manitoLetterRepository.findById(manitoLetterId)
        .orElseThrow(() -> new EntityNotFoundException(ManitoErrorMessages.MANITO_LETTER_NOT_FOUND.getMessage()));

    manitoLetter.reportLetter(userNickname);

    Report report = Report.builder()
        .reportType(requestDto.getReportType())
        .userId(user.getUserId())
        .type(ReportObjectType.MANITO_LETTER)
        .reportObjectId(manitoLetterId)
        .build();

    reportRepository.save(report);
  }

  // 마니또 답장 신고
  public void reportManitoAnswer(Long manitoLetterId, String userNickname, ReportRequestDto requestDto) {
    User user = userRepository.findUserByNickname(userNickname)
        .orElseThrow(() -> new EntityNotFoundException(ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    ManitoLetter manitoLetter = manitoLetterRepository.findById(manitoLetterId)
        .orElseThrow(() -> new EntityNotFoundException(ManitoErrorMessages.MANITO_LETTER_NOT_FOUND.getMessage()));

    manitoLetter.reportAnswer(userNickname);

    Report report = Report.builder()
        .reportType(requestDto.getReportType())
        .userId(user.getUserId())
        .type(ReportObjectType.MANITO_ANSWER)
        .reportObjectId(manitoLetterId)
        .build();

    reportRepository.save(report);
  }

}

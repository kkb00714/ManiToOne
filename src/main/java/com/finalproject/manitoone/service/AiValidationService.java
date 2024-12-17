package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.AiPostLog;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.repository.AiPostLogRepository;
import com.finalproject.manitoone.util.AlanUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiValidationService {
  private final AiPostLogRepository aiPostLogRepository;

  @Transactional
  public boolean validatePostContent(Post post) {
    // 이미 검증된 게시물인지 확인
    Optional<AiPostLog> existingLog = aiPostLogRepository.findByPostPostId(post.getPostId());
    if (existingLog.isPresent()) {
      return post.getIsManito(); // 이미 검증된 게시물은 현재 상태 반환
    }

    try {
      // AI 검증 요청
      String aiResponse = AlanUtil.getAlanAnswer(
          "다음 게시물이 마니또 서비스에서 다른 유저에게 전달되어 답신을 받기 적절한 내용인지 true 또는 false로만 답변해주세요. " +
              "보편적인 사용자가 답변을 하기 어려울 정도로 무의미한 내용만으로 이루어져 있거나, 폭력적이거나 선정적인 내용, 혐오 표현, 차별적 내용이 있다면 false를 반환하세요: " +
              post.getContent()
      );

      // AI 응답 저장
      boolean isAppropriate = aiResponse.toLowerCase().contains("true");
      aiPostLogRepository.save(AiPostLog.builder()
          .post(post)
          .aiContent(aiResponse)
          .build());

      return isAppropriate;

    } catch (Exception e) {
      log.error("AI validation failed for post ID: {}", post.getPostId(), e);
      return true; // AI 검증 실패 시 기본적으로 허용
    }
  }

}

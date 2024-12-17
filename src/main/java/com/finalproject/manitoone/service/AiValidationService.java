package com.finalproject.manitoone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.manitoone.domain.AiPostLog;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.repository.AiPostLogRepository;
import com.finalproject.manitoone.repository.PostRepository;
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
  private final PostRepository postRepository;

  @Transactional
  public boolean validatePostContent(Post post) {
    Optional<AiPostLog> existingLog = aiPostLogRepository.findByPostPostId(post.getPostId());
    if (existingLog.isPresent()) {
      log.info("Using cached AI validation result for post ID: {}", post.getPostId());
      return post.getIsManito();
    }

    try {
      String aiResponse = AlanUtil.getValidationAnswer(post.getContent());

      aiResponse = aiResponse.replaceAll("```json\\s*", "")
          .replaceAll("```\\s*$", "")
          .trim();

      log.debug("AI Response after cleanup: {}", aiResponse);

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode responseNode = objectMapper.readTree(aiResponse);
      boolean isAppropriate = responseNode.get("isValid").asBoolean();

      // AI 검증 결과 저장
      aiPostLogRepository.save(AiPostLog.builder()
          .post(post)
          .aiContent(aiResponse)
          .build());

      // 게시물 상태 업데이트 및 저장
      post.updateManitoStatus(isAppropriate);
      postRepository.save(post);

      log.info("AI validation completed for post ID: {}, result: {}, updating isManito to: {}",
          post.getPostId(), isAppropriate, isAppropriate);

      return isAppropriate;

    } catch (Exception e) {
      log.error("AI validation failed for post ID: {}", post.getPostId(), e);

      try {
        aiPostLogRepository.save(AiPostLog.builder()
            .post(post)
            .aiContent("Validation failed: " + e.getMessage())
            .build());

        // 실패 시 게시물을 부적절로 표시하고 저장
        post.updateManitoStatus(false);
        postRepository.save(post);

        log.info("AI validation failed, setting isManito to false for post ID: {}", post.getPostId());
      } catch (Exception saveError) {
        log.error("Failed to save AI validation log for post ID: {}",
            post.getPostId(), saveError);
      }

      return false;
    }
  }
}

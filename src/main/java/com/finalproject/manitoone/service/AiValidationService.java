package com.finalproject.manitoone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.manitoone.domain.AiValidationLog;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.repository.AiValidationLogRepository;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.util.AlanUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiValidationService {
  private final AiValidationLogRepository aiValidationLogRepository;
  private final PostRepository postRepository;
  private final ObjectMapper objectMapper;
  private final ValidationResultHandler validationResultHandler;

  @Transactional(
      propagation = Propagation.REQUIRES_NEW,
      isolation = Isolation.REPEATABLE_READ
  )
  public boolean validatePostContent(Post post) {
    try {
      Optional<AiValidationLog> existingLog = aiValidationLogRepository.findByPostPostId(post.getPostId());
      if (existingLog.isPresent()) {
        log.info("Using cached AI validation result for post ID: {}", post.getPostId());
        return existingLog.get().isValidationResult();
      }

      String aiResponse = AlanUtil.getValidationAnswer(post.getContent());
      JsonNode validationNode = objectMapper.readTree(aiResponse);
      boolean isAppropriate = validationNode.path("isValid").asBoolean();

      validationResultHandler.saveValidationResult(post, isAppropriate, aiResponse);

      log.info("AI validation completed for post ID: {}, result: {}, thread: {}",
          post.getPostId(), isAppropriate, Thread.currentThread().getName());

      return isAppropriate;

    } catch (Exception e) {
      log.error("AI validation failed for post ID: {}, thread: {}",
          post.getPostId(), Thread.currentThread().getName(), e);
      validationResultHandler.handleValidationError(post, e);
      return false;
    }
  }
}

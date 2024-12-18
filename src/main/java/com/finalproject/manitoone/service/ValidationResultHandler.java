package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.AiValidationLog;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.repository.AiValidationLogRepository;
import com.finalproject.manitoone.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationResultHandler {
  private final AiValidationLogRepository aiValidationLogRepository;
  private final PostRepository postRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveValidationResult(Post post, boolean isValid, String aiResponse) {
    try {
      AiValidationLog validationLog = AiValidationLog.builder()
          .post(post)
          .validationResult(isValid)
          .rawAiResponse(aiResponse)
          .build();

      aiValidationLogRepository.save(validationLog);

      post.updateManitoStatus(isValid);
      postRepository.save(post);

      log.debug("Validation result saved for post ID: {}, thread: {}",
          post.getPostId(), Thread.currentThread().getName());

    } catch (Exception e) {
      log.error("Failed to save validation result for post ID: {}, thread: {}",
          post.getPostId(), Thread.currentThread().getName(), e);
      throw new RuntimeException("Failed to save validation result", e);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleValidationError(Post post, Exception error) {
    try {
      saveValidationResult(post, false, "Validation error: " + error.getMessage());
    } catch (Exception e) {
      log.error("Failed to handle validation error for post ID: {}, thread: {}",
          post.getPostId(), Thread.currentThread().getName(), e);
    }
  }
}

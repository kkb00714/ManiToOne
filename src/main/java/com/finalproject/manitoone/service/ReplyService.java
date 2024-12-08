package com.finalproject.manitoone.service;

import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.ReplyPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyService {

  private final PostRepository postRepository;
  private final ReplyPostRepository replyPostRepository;
}

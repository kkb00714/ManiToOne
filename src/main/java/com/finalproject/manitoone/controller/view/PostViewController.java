package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.service.PostService;
import com.finalproject.manitoone.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostViewController {

  private final PostService postService;
  private final ReplyService replyService;
}

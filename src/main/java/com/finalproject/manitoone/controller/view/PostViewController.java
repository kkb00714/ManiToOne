package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.service.PostService;
import com.finalproject.manitoone.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostViewController {

  private final PostService postService;
  private final ReplyService replyService;

  // 게시글 상세 조회
  @GetMapping("/{postId}")
  public String getPostDetail(@PathVariable("postId") Long postId, Model model) {
    model.addAttribute("post", postService.getPostDetail(postId));
    return "pages/post/postDetail";
  }
}

package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.domain.dto.ReplyResponseDto;
import com.finalproject.manitoone.service.PostService;
import com.finalproject.manitoone.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PostViewController {

  private final PostService postService;
  private final ReplyService replyService;

  // 게시글 상세 조회
  @GetMapping("/post/{postId}")
  public String getPostDetail(@PathVariable("postId") Long postId,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
      Model model) {
    model.addAttribute("post", postService.getPostDetail(postId));
    model.addAttribute("postLikesNum", postService.getPostLikesNum(postId));
    model.addAttribute("postRepliesNum", replyService.getRepliesNum(postId));
    model.addAttribute("replies", replyService.getReplies(postId, pageable));
    return "pages/post/postDetail";
  }

  // 답글 상세 조회
  @GetMapping("/reply/{replyId}")
  public String getReplyDetail(@PathVariable("replyId") Long replyId,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
      Model model) {
    ReplyResponseDto reply = replyService.getReply(replyId);
    model.addAttribute("post", reply.getPost());
    model.addAttribute("postLikesNum", postService.getPostLikesNum(reply.getPost().getPostId()));
    model.addAttribute("postRepliesNum", replyService.getRepliesNum(reply.getPost().getPostId()));
    model.addAttribute("reply", reply);
    model.addAttribute("rereplies", replyService.getReReplies(reply.getReplyPostId(), pageable));
    return "pages/post/replyDetail";
  }
}
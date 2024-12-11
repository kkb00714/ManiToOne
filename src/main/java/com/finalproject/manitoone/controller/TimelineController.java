package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TimelineController {
  private final PostService postService;

  @GetMapping("/timeline")
  public ResponseEntity<Page<PostViewResponseDto>> getTimelinePosts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      HttpSession session) {  // UserDetails 대신 HttpSession 사용

    String nickname = (String) session.getAttribute("nickname");
    if (nickname == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, "createdAt"));
    Page<PostViewResponseDto> posts = postService.getTimelinePosts(nickname, pageRequest);
    return ResponseEntity.ok(posts);
  }

//  @GetMapping("/timeline")
//  public ResponseEntity<Page<PostViewResponseDto>> getTimelinePosts(
//      @RequestParam(defaultValue = "0") int page,
//      @RequestParam(defaultValue = "20") int size,
//      @AuthenticationPrincipal UserDetails userDetails) {
//
//    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, "createdAt"));
//    Page<PostViewResponseDto> posts = postService.getTimelinePosts(userDetails.getUsername(), pageRequest);
//    return ResponseEntity.ok(posts);
//  }

}

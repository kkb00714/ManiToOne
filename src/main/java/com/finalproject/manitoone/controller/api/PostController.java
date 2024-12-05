package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.domain.dto.AddPostRequestDto;
import com.finalproject.manitoone.domain.dto.PostResponseDto;
import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.service.PostService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/post")
public class PostController {

  private final PostService postService;

  // 게시글 생성
  @PostMapping
  public ResponseEntity<PostResponseDto> createPost(@RequestBody AddPostRequestDto request) {
    PostResponseDto post = postService.createPost(request);
    log.info("request: {}, {}", request.getContent(), request.getIsManito());
    log.info("response: {}, {}", post.getContent(), post.getIsManito());
    return ResponseEntity.status(HttpStatus.CREATED).body(post);
  }

//  public ResponseEntity<PostResponseDto> createPost(@RequestBody AddPostRequestDto request,
//      @AuthenticationPrincipal User user) {
//    try {
//      postService.createPost(request, user);
//      return ResponseEntity.status(HttpStatus.CREATED).build();
//    } catch (Exception e) {
//      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//    }
//  }

  @GetMapping("/by/{nickName}")
  public ResponseEntity<List<PostViewResponseDto>> getPostsByUserId(@PathVariable String nickName,
      @PageableDefault(sort = "postId", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(postService.getPostsByNickName(nickName, pageable));
  }

  @GetMapping("/{nickName}/liked")
  public ResponseEntity<List<PostViewResponseDto>> getLikedPostsByUserId(
      @PathVariable String nickName,
      @PageableDefault(direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(postService.getLikePostByNickName(nickName, pageable));
  }

  @GetMapping("/hidden")
  public ResponseEntity<List<PostViewResponseDto>> getPostById(
      @PageableDefault(sort = "postId", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(postService.getMyHiddenPosts("테스트1", pageable));
  }
}

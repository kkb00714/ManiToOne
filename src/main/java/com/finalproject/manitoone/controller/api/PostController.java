package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.domain.dto.AddPostRequestDto;
import com.finalproject.manitoone.domain.dto.AddReportRequestDto;
import com.finalproject.manitoone.domain.dto.PostResponseDto;
import com.finalproject.manitoone.domain.dto.ReportResponseDto;
import com.finalproject.manitoone.domain.dto.UpdatePostRequestDto;
import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.service.PostService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

  private final PostService postService;

  // 게시글 생성
  // TODO: 이미지 업로드
  @PostMapping
  public ResponseEntity<PostResponseDto> createPost(@RequestBody AddPostRequestDto request,
      HttpSession session) {
    String email = session.getAttribute("email") + "";

    System.out.println("email: " + email);
    System.out.println("content: " + request.getContent());
    System.out.println(request.getIsManito());

    return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request, email));
  }

  // 게시글 수정
  // TODO: 이미지 수정
  @PutMapping("/{postId}")
  public ResponseEntity<PostResponseDto> updatePost(@PathVariable("postId") Long postId,
      @RequestBody UpdatePostRequestDto request,
      HttpSession session) {
    String email = session.getAttribute("email") + "";

    return ResponseEntity.ok(postService.updatePost(postId, request, email));
  }

  // 모든 게시글 조회
  @GetMapping
  public ResponseEntity<Page<PostResponseDto>> getPosts(
      @PageableDefault(sort = "postId", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<PostResponseDto> posts = postService.getPosts(pageable);
    return ResponseEntity.ok(posts);
  }

  // 게시글 상세 조회
  // TODO: 이미지 조회
  @GetMapping("/{postId}")
  public ResponseEntity<PostResponseDto> getPostDetail(@PathVariable("postId") Long postId) {
    PostResponseDto post = postService.getPostDetail(postId);
    return ResponseEntity.ok(post);
  }

//  public CompletableFuture<ResponseEntity<PostResponseDto>> createPost(
//      @RequestBody AddPostRequestDto request,
//      @AuthenticationPrincipal User user) {
//    PostResponseDto post = postService.createPost(request, user);
//    return CompletableFuture.supplyAsync(
//        () -> ResponseEntity.status(HttpStatus.CREATED).body(post));
//  }

  // 게시글 삭제
  @DeleteMapping("/{postId}")
  public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId, HttpSession session) {
    String email = (String) session.getAttribute("email");
    postService.deletePost(postId, email);
    return ResponseEntity.ok().build();
  }

  // 게시글 숨기기
  @PutMapping("/hidden/{postId}")
  public ResponseEntity<Void> hidePost(@PathVariable("postId") Long postId) {
    postService.hidePost(postId);
    return ResponseEntity.ok().build();
  }

  // 게시글 좋아요
  @PostMapping("/like/{postId}")
  public ResponseEntity<PostResponseDto> likePost(@PathVariable("postId") Long postId,
      HttpSession session) {
    String email = session.getAttribute("email") + "";

    PostResponseDto post = postService.likePost(postId, email);
    return ResponseEntity.ok(post);
  }

  // 게시글 좋아요 개수 조회
  @GetMapping("/like/number/{postId}")
  public ResponseEntity<Integer> getPostLikesNum(@PathVariable("postId") Long postId) {
    Integer num = postService.getPostLikesNum(postId);
    return ResponseEntity.ok(num);
  }

  // 게시글 신고
  @PutMapping("/report/{postId}")
  public ResponseEntity<ReportResponseDto> reportPost(@PathVariable("postId") Long postId,
      @RequestBody AddReportRequestDto request,
      HttpSession session) {
    String email = session.getAttribute("email") + "";

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(postService.reportPost(postId, request, email));
  }

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
      @PageableDefault(sort = "postId", direction = Sort.Direction.DESC) Pageable pageable,
      HttpSession session) {
    return ResponseEntity.ok(
        postService.getMyHiddenPosts((String) session.getAttribute("nickname"), pageable));
  }
}

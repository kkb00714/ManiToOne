package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.PostImage;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.AddPostRequestDto;
import com.finalproject.manitoone.domain.dto.PostResponseDto;
import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.dto.postimage.PostImageResponseDto;
import com.finalproject.manitoone.repository.PostImageRepository;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.UserPostLikeRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final PostImageRepository postImageRepository;
  private final UserPostLikeRepository userPostLikeRepository;

  // 게시글 생성
  public PostResponseDto createPost(AddPostRequestDto request, User user) throws IOException {
    // text 저장
    Post post = Post.builder()
        .user(user)
        .content(request.getContent())
        .isManito(request.getIsManito())
        .build();

    postRepository.save(post);

    // 이미지 저장
    List<MultipartFile> images = request.getImages();

    if (images != null && !images.isEmpty()) {
      for (MultipartFile image : images) {
        saveImage(post, image);
      }
    }

    return post.toPostResponseDto();
  }

  // 이미지 저장
  private void saveImage(Post post, MultipartFile image) throws IOException {
    // 파일 저장 경로 지정
    String uploadDir = "src/main/resources/static/img/upload/";
    Path uploadPath = Paths.get(uploadDir);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    // 이미지 저장
    String originalFilename = image.getOriginalFilename();
    String uniqueFileName = UUID.randomUUID() + "-" + originalFilename;
    Path filePath = uploadPath.resolve(uniqueFileName);
    Files.write(filePath, image.getBytes());

    postImageRepository.save(PostImage.builder()
        .fileName(originalFilename)
        .post(post)
        .build());
  }

  public List<PostViewResponseDto> getPostsByNickName(String nickName, Pageable pageable) {
    // TODO: 내 게시글인지는 어떻게 판별할까요?
    List<Post> posts = postRepository.findAllByIsBlindFalseAndIsHiddenFalseAndUser_Nickname(
            nickName,
            pageable)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 유저 ID를 찾을 수 없습니다."));

    List<PostViewResponseDto> postResponses = posts.stream()
        .map(Post::toPostViewResponseDto)
        .toList();

    return addAdditionalDataToDto(postResponses);
  }

  public List<PostViewResponseDto> getLikePostByNickName(String nickName, Pageable pageable) {
    List<PostViewResponseDto> postResponses = userPostLikeRepository.findAllByUser_nicknameAndPost_IsHiddenFalseAndPost_IsBlindFalse(
            nickName, pageable)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 유저 ID를 찾을 수 없습니다."))
        .stream()
        .map(userPostLike -> new PostViewResponseDto(
            userPostLike.getPost().getPostId(),
            userPostLike.getUser().getUserId(),
            userPostLike.getPost().getContent(),
            userPostLike.getPost().getCreatedAt(),
            userPostLike.getPost().getUpdatedAt(),
            null,
            null)
        ).toList();

    return addAdditionalDataToDto(postResponses);
  }

  public List<PostViewResponseDto> getMyHiddenPosts(String nickName, Pageable pageable) {
    List<PostViewResponseDto> postResponses = postRepository.findAllByIsBlindFalseAndIsHiddenTrueAndUser_Nickname(
            nickName, pageable)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 유저 ID를 찾을 수 없습니다."))
        .stream()
        .map(Post::toPostViewResponseDto)
        .toList();

    return addAdditionalDataToDto(postResponses);
  }

  private List<PostViewResponseDto> addAdditionalDataToDto(List<PostViewResponseDto> postResponses) {
    postResponses.forEach(postResponseDto -> {
      List<PostImageResponseDto> postImages = postImageRepository.findAllByPost_PostId(
              postResponseDto.getPostId())
          .orElseThrow(() -> new IllegalArgumentException("해당하는 포스트 ID를 찾을 수 없습니다."))
          .stream()
          .map(postImage -> new PostImageResponseDto(postImage.getFileName()))  // 변환
          .toList();
      Integer likeCount = userPostLikeRepository.countAllByPost_PostId(postResponseDto.getPostId())
          .orElseThrow(() -> new IllegalArgumentException("해당하는 포스트 ID를 찾을 수 없습니다."));
      postResponseDto.addLikeCount(likeCount);
      postResponseDto.addPostImages(postImages);
    });

    return postResponses;
  }
}

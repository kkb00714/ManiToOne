package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.PostImage;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.AddPostRequestDto;
import com.finalproject.manitoone.domain.dto.PostResponseDto;
import com.finalproject.manitoone.repository.PostImageRepository;
import com.finalproject.manitoone.repository.PostRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final PostImageRepository postImageRepository;

  // 게시글 생성
  public PostResponseDto createPost(AddPostRequestDto request, User user) throws IOException {
    // text 저장
    Post post = Post.builder()
        .user(user)
        .content(request.getContent())
        .isManito(request.getIsManito())
        .build();

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
}

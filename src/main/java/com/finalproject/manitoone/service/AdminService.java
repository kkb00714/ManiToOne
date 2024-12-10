package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.QPost;
import com.finalproject.manitoone.domain.QUser;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.admin.PostSearchRequestDto;
import com.finalproject.manitoone.domain.dto.admin.PostSearchResponseDto;
import com.finalproject.manitoone.domain.dto.admin.UserProfileRequestDto;
import com.finalproject.manitoone.domain.dto.admin.UserProfileResponseDto;
import com.finalproject.manitoone.domain.dto.admin.UserSearchRequestDto;
import com.finalproject.manitoone.domain.dto.admin.UserSearchResponseDto;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.util.FileUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminService {

  private static final String PROFILE_IMAGE_DIR = "/home/profile/images/";
  private static final String TEST_DIR = "C:\\test_image\\";

  private final JPAQueryFactory queryFactory;

  private final UserRepository userRepository;
  private final PostRepository postRepository;

  private final FileUtil fileUtil;

  public Page<UserSearchResponseDto> searchUsers(UserSearchRequestDto userSearchRequestDto,
      Pageable pageable) {
    QUser user = QUser.user;

    BooleanBuilder builder = new BooleanBuilder();

    if (userSearchRequestDto.getNickname() != null && !userSearchRequestDto.getNickname()
        .isEmpty()) {
      builder.and(user.nickname.containsIgnoreCase(userSearchRequestDto.getNickname()));
    }

    if (userSearchRequestDto.getEmail() != null && !userSearchRequestDto.getEmail().isEmpty()) {
      builder.and(user.email.containsIgnoreCase(userSearchRequestDto.getEmail()));
    }

    if (userSearchRequestDto.getName() != null && !userSearchRequestDto.getName().isEmpty()) {
      builder.and(user.name.containsIgnoreCase(userSearchRequestDto.getName()));
    }

    if (userSearchRequestDto.getStatus() != null) {
      builder.and(user.status.eq(userSearchRequestDto.getStatus()));
    }

    // QueryDSL로 페이징 처리
    List<User> users = queryFactory
        .selectFrom(user)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(user.userId.asc())
        .fetch();

    long total = queryFactory
        .selectFrom(user)
        .where(builder)
        .fetchCount();

    List<UserSearchResponseDto> dtoList = users.stream()
        .map(this::toUserSearchResponseDto)
        .toList();

    return new PageImpl<>(dtoList, pageable, total);
  }

  private UserSearchResponseDto toUserSearchResponseDto(User user) {
    return UserSearchResponseDto.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .name(user.getName())
        .nickname(user.getNickname())
        .birth(user.getBirth())
        .introduce(user.getIntroduce())
        .profileImage(user.getProfileImage())
        .status(user.getStatus())
        .role(user.getRole())
        .unbannedAt(user.getUnbannedAt())
        .createdAt(user.getCreatedAt())
        .build();
  }

  public UserProfileResponseDto updateUser(UserProfileRequestDto userProfileRequestDto) {
    User user = userRepository.findById(userProfileRequestDto.getUserId())
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

    if (userProfileRequestDto.getEmail() != null) {
      if (isDuplicateEmail(userProfileRequestDto.getEmail())) {
        throw new IllegalArgumentException(IllegalActionMessages.EMAIL_ALREADY_IN_USE.getMessage());
      }
      user = user.toBuilder()
          .email(userProfileRequestDto.getEmail())
          .build();
    }
    if (userProfileRequestDto.getName() != null) {
      user = user.toBuilder()
          .name(userProfileRequestDto.getName())
          .build();
    }
    if (userProfileRequestDto.getNickname() != null) {
      if (isDuplicateNickname(userProfileRequestDto.getNickname())) {
        throw new IllegalArgumentException(
            IllegalActionMessages.NICKNAME_ALREADY_IN_USE.getMessage());
      }
      user = user.toBuilder()
          .nickname(userProfileRequestDto.getNickname())
          .build();
    }
    if (userProfileRequestDto.getBirth() != null) {
      user = user.toBuilder()
          .birth(userProfileRequestDto.getBirth())
          .build();
    }
    if (userProfileRequestDto.getIntroduce() != null) {
      user = user.toBuilder()
          .introduce(userProfileRequestDto.getIntroduce())
          .build();
    }
    if (userProfileRequestDto.getProfileImage() != null) {
      user = user.toBuilder()
          .profileImage(userProfileRequestDto.getProfileImage())
          .build();
    }
    if (userProfileRequestDto.getStatus() != null) {
      user = user.toBuilder()
          .status(userProfileRequestDto.getStatus())
          .build();
    }
    if (userProfileRequestDto.getRole() != null) {
      user = user.toBuilder()
          .role(userProfileRequestDto.getRole())
          .build();
    }
    if (Boolean.TRUE.equals(userProfileRequestDto.getClearUnbannedAt())) {
      user = user.toBuilder()
          .unbannedAt(null)
          .build();
    } else {
      if (userProfileRequestDto.getUnbannedAt() != null) {
        user = user.toBuilder()
            .unbannedAt(userProfileRequestDto.getUnbannedAt())
            .build();
      }
    }

    return toUserProfileResponseDto(userRepository.save(user));
  }

  private UserProfileResponseDto toUserProfileResponseDto(User user) {
    return UserProfileResponseDto.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .name(user.getName())
        .nickname(user.getNickname())
        .birth(user.getBirth())
        .introduce(user.getIntroduce())
        .profileImage(user.getProfileImage())
        .status(user.getStatus())
        .role(user.getRole())
        .unbannedAt(user.getUnbannedAt())
        .createdAt(user.getCreatedAt())
        .build();
  }

  private boolean isDuplicateEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  private boolean isDuplicateNickname(String nickname) {
    return userRepository.existsByNickname(nickname);
  }

  public UserProfileResponseDto updateProfileImage(Long userId, MultipartFile profileImageFile) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

    if (user.getProfileImage() != null && !user.isDefaultImage()) {
      String existingImagePath = user.getProfileImage(); // 기존 이미지의 절대 경로를 구성
      File existingImageFile = new File(existingImagePath);

      if (existingImageFile.exists()) {
        fileUtil.cleanUp(Paths.get(existingImagePath));
      }
    }

    if (profileImageFile == null) {
      user.updateDefaultImage();
    } else {
      String uploadDir = TEST_DIR;
      File uploadDirectory = new File(uploadDir);

      if (!uploadDirectory.exists()) {
        fileUtil.createDir(Paths.get(uploadDir));
      }

      String uniqueFileName = UUID.randomUUID() + "_" + profileImageFile.getOriginalFilename();
      String finalFilePath = uploadDir + uniqueFileName;

      fileUtil.save(Paths.get(finalFilePath), profileImageFile);
      user.updateProfileImage(finalFilePath);
    }
    return toUserProfileResponseDto(userRepository.save(user));
  }

  public Page<PostSearchResponseDto> searchPosts(PostSearchRequestDto postSearchRequestDto,
      Pageable pageable) {
    QPost post = QPost.post;

    BooleanBuilder builder = new BooleanBuilder();

    // nickname 조건 (User 기반 검색)
    if (postSearchRequestDto.getNickname() != null && !postSearchRequestDto.getNickname()
        .isEmpty()) {
      builder.and(post.user.nickname.containsIgnoreCase(postSearchRequestDto.getNickname()));
    }

    // name 조건 (User 기반 검색)
    if (postSearchRequestDto.getName() != null && !postSearchRequestDto.getName().isEmpty()) {
      builder.and(post.user.name.containsIgnoreCase(postSearchRequestDto.getName()));
    }

    // email 조건 (User 기반 검색)
    if (postSearchRequestDto.getEmail() != null && !postSearchRequestDto.getEmail().isEmpty()) {
      builder.and(post.user.email.containsIgnoreCase(postSearchRequestDto.getEmail()));
    }

    // content 조건 (Post 기반 검색)
    if (postSearchRequestDto.getContent() != null && !postSearchRequestDto.getContent().isEmpty()) {
      builder.and(post.content.containsIgnoreCase(postSearchRequestDto.getContent()));
    }

    // isBlind 조건 (Post 기반 검색)
    if (postSearchRequestDto.getIsBlind() != null) {
      builder.and(post.isBlind.eq(postSearchRequestDto.getIsBlind()));
    }

    // QueryDSL로 페이징 처리
    List<Post> posts = queryFactory
        .selectFrom(post)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(post.postId.asc())
        .fetch();

    long total = queryFactory
        .selectFrom(post)
        .where(builder)
        .fetchCount();

    List<PostSearchResponseDto> dtoList = posts.stream()
        .map(this::toPostSearchResponseDto)
        .toList();

    return new PageImpl<>(dtoList, pageable, total);
  }

  private PostSearchResponseDto toPostSearchResponseDto(Post post) {
    return PostSearchResponseDto.builder()
        .postId(post.getPostId())
        .user(toUserSearchResponseDto(post.getUser()))
        .content(post.getContent())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .isManito(post.getIsManito())
        .isSelected(post.getIsSelected())
        .isHidden(post.getIsHidden())
        .isBlind(post.getIsBlind())
        .build();
  }

  public PostSearchResponseDto updateBlind(Long postId) {
    Post post = postRepository.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    post.updateBlind();
    return toPostSearchResponseDto(postRepository.save(post));
  }
}

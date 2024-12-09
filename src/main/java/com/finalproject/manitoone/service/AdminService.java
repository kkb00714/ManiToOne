package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.QUser;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.admin.UserProfileRequestDto;
import com.finalproject.manitoone.domain.dto.admin.UserProfileResponseDto;
import com.finalproject.manitoone.domain.dto.admin.UserSearchRequestDto;
import com.finalproject.manitoone.domain.dto.admin.UserSearchResponseDto;
import com.finalproject.manitoone.repository.UserRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final JPAQueryFactory queryFactory;

  private final UserRepository userRepository;

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
        .orElseThrow(() -> new IllegalArgumentException(IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

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
        throw new IllegalArgumentException(IllegalActionMessages.NICKNAME_ALREADY_IN_USE.getMessage());
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
    if (userProfileRequestDto.getClearUnbannedAt()) {
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
}

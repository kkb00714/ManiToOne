package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.QUser;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.admin.UserSearchRequestDto;
import com.finalproject.manitoone.domain.dto.admin.UserSearchResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

  private final JPAQueryFactory queryFactory;

  public AdminService(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  public Page<UserSearchResponseDto> searchUsers(UserSearchRequestDto userSearchRequestDto, Pageable pageable) {
    QUser user = QUser.user;

    BooleanBuilder builder = new BooleanBuilder();

    if (userSearchRequestDto.getNickname() != null && !userSearchRequestDto.getNickname().isEmpty()) {
      builder.and(user.nickname.containsIgnoreCase(userSearchRequestDto.getNickname()));
    }

    if (userSearchRequestDto.getEmail() != null && !userSearchRequestDto.getEmail().isEmpty()) {
      builder.and(user.email.containsIgnoreCase(userSearchRequestDto.getEmail()));
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
}

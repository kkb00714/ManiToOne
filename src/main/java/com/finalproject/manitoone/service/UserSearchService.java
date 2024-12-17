package com.finalproject.manitoone.service;

import com.finalproject.manitoone.dto.user.MainUserSearchResponseDto;
import com.finalproject.manitoone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSearchService {

  private final UserRepository userRepository;

  public Page<MainUserSearchResponseDto> searchUsers(String query, Pageable pageable) {
    return userRepository.findUsersByNicknameStartsWith(query, pageable);
  }
}

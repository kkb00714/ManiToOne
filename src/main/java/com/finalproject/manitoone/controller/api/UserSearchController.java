package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.dto.user.MainUserSearchResponseDto;
import com.finalproject.manitoone.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserSearchController {

  private final UserSearchService userSearchService;

  @GetMapping("/user/search")
  public ResponseEntity<Object> getUsers(
      @RequestParam(required = false, defaultValue = "") String query,
      @PageableDefault(size = 7, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable) {
    Page<MainUserSearchResponseDto> userPage = userSearchService.searchUsers(query, pageable);
    return ResponseEntity.ok(userPage);
  }
}

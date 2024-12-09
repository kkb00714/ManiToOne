package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.domain.dto.admin.UserProfileRequestDto;
import com.finalproject.manitoone.domain.dto.admin.UserProfileResponseDto;
import com.finalproject.manitoone.domain.dto.admin.UserSearchRequestDto;
import com.finalproject.manitoone.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminApiController {

  private final AdminService adminService;

  @PostMapping("/users")
  public ResponseEntity<Object> getAllUsers(
      @PageableDefault(size = 2, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
      @RequestBody UserSearchRequestDto userSearchRequestDto) {
    return ResponseEntity.ok(adminService.searchUsers(userSearchRequestDto, pageable));
  }

  @PutMapping("/users")
  public ResponseEntity<Object> updateUsers(
      @RequestBody UserProfileRequestDto userProfileRequestDto) {
    UserProfileResponseDto updatedUser = adminService.updateUser(userProfileRequestDto);
    return ResponseEntity.ok(updatedUser);
  }

  @PutMapping("/users/{userId}")
  public ResponseEntity<Object> updateUserProfileImage(@PathVariable Long userId,
      @RequestPart(required = false) MultipartFile profileImageFile) {
    return ResponseEntity.ok(adminService.updateProfileImage(userId, profileImageFile));
  }
}

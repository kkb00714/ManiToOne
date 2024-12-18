package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.constants.ReportType;
import com.finalproject.manitoone.constants.SearchType;
import com.finalproject.manitoone.domain.dto.admin.ReportSearchRequestDto;
import com.finalproject.manitoone.domain.dto.admin.UserProfileRequestDto;
import com.finalproject.manitoone.service.AdminService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/api")
@RequiredArgsConstructor
public class AdminApiController {

  private final AdminService adminService;

  @GetMapping("/users")
  public ResponseEntity<Object> getAllUsers(
      @PageableDefault(size = 5, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
      @RequestParam(defaultValue = "") SearchType type,
      @RequestParam(defaultValue = "") Integer status,
      @RequestParam(defaultValue = "") String content) {
    return ResponseEntity.ok(adminService.searchUsers(type, content, status, pageable));
  }

  @PutMapping("/users")
  public ResponseEntity<Object> updateUsers(
      @RequestBody UserProfileRequestDto userProfileRequestDto) {
    return ResponseEntity.ok(adminService.updateUser(userProfileRequestDto));
  }

  @PutMapping("/users/{userId}")
  public ResponseEntity<Object> updateUserProfileImage(@PathVariable Long userId,
      @RequestPart(required = false) MultipartFile profileImageFile) throws IOException {
    return ResponseEntity.ok(adminService.updateProfileImage(userId, profileImageFile));
  }

  @GetMapping("/posts")
  public ResponseEntity<Object> getAllPosts(
      @PageableDefault(size = 5, sort = "postId", direction = Sort.Direction.ASC) Pageable pageable,
      @RequestParam(defaultValue = "") SearchType type,
      @RequestParam(defaultValue = "") String content,
      @RequestParam(defaultValue = "") Boolean isBlind) {
    return ResponseEntity.ok(adminService.searchPosts(type, content, isBlind, pageable));
  }

  @GetMapping("/post/{postId}/image")
  public ResponseEntity<Object> getPostImages(@PathVariable Long postId) {
    return ResponseEntity.ok(adminService.getPostImages(postId));
  }

  @PutMapping("/blind/post/{postId}")
  public ResponseEntity<Object> blindPost(@PathVariable Long postId) {
    return ResponseEntity.ok(adminService.updateBlind(postId));
  }

  @PutMapping("/blind/reply/{replyPostId}")
  public ResponseEntity<Object> blindReply(@PathVariable Long replyPostId) {
    return ResponseEntity.ok(adminService.updateBlindReply(replyPostId));
  }

  @DeleteMapping("/post/{postId}")
  public ResponseEntity<Object> deletePost(@PathVariable Long postId) {
    adminService.deletePost(postId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/reply/{replyPostId}")
  public ResponseEntity<Object> deleteReply(@PathVariable Long replyPostId) {
    adminService.deleteReply(replyPostId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reports")
  public ResponseEntity<Object> getReports (
      @PageableDefault(size = 2, sort = "reportId", direction = Sort.Direction.ASC) Pageable pageable,
      @RequestParam(defaultValue = "") SearchType type,
      @RequestParam(defaultValue = "") String content,
      @RequestParam(defaultValue = "") ReportObjectType reportObjectType,
      @RequestParam(defaultValue = "") ReportType reportType) {
    return ResponseEntity.ok(adminService.searchReports(type, content, reportObjectType, reportType, pageable));
  }

  @GetMapping("/report/post/{postId}")
  public ResponseEntity<Object> isReportPost(@PathVariable Long postId) {
    return ResponseEntity.ok(adminService.isReportPost(postId));
  }

  @GetMapping("/report/reply/{replyPostId}")
  public ResponseEntity<Object> isReportReply(@PathVariable Long replyPostId) {
    return ResponseEntity.ok(adminService.isReportReply(replyPostId));
  }

  @DeleteMapping("/report/{reportId}")
  public ResponseEntity<Object> deleteReport(@PathVariable Long reportId) {
    adminService.deleteReport(reportId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/manito/reports")
  public ResponseEntity<Object> getManitoReports (
      @PageableDefault(size = 2, sort = "reportId", direction = Sort.Direction.ASC) Pageable pageable,
      @RequestBody ReportSearchRequestDto reportSearchRequestDto) {
    return ResponseEntity.ok(adminService.searchManitoReports(reportSearchRequestDto, pageable));
  }
}
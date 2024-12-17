package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class S3UploadController {

  private final S3Service s3Service;


  @PostMapping("/upload")
  public String uploadFile(@RequestParam("file") MultipartFile file) {
    try {
      String imageUrl = s3Service.uploadImage(file);
      return "파일 업로드 성공!" + imageUrl;
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  @PostMapping("/update-profile-image")
  public ResponseEntity<String> updateProfileImage(
      @RequestParam("email") String email,
      @RequestParam("file") MultipartFile file) {

    try {
      String newImageUrl = s3Service.updateProfileImage(email, file);
      return ResponseEntity.ok("프로필 사진이 업데이트되었습니다: " + newImageUrl);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("프로필 사진 업데이트 실패: " + e.getMessage());
    }
  }
}

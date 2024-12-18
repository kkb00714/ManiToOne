package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.service.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
      HttpServletRequest request,
      @RequestParam("file") MultipartFile file) {
    try {
      HttpSession session = request.getSession(false);
      if (session == null || session.getAttribute("email") == null) {
        return ResponseEntity.status(401).body("로그인이 필요합니다.");
      }

      String loggedInEmail = (String) session.getAttribute("email");

      String newImageUrl = s3Service.updateProfileImage(loggedInEmail, file);

      session.setAttribute("profileImage", newImageUrl);

      return ResponseEntity.ok(newImageUrl);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("프로필 사진 업데이트 실패: " + e.getMessage());
    }
  }
}

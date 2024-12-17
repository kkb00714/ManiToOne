package com.finalproject.manitoone.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.repository.UserRepository;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3Service {

  private final AmazonS3 amazonS3;
  private final UserRepository userRepository;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;


  public String uploadImage(MultipartFile image) throws IOException {
    String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(image.getContentType());
    metadata.setContentLength(image.getSize());

    PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName,
        image.getInputStream(), metadata);

    amazonS3.putObject(putObjectRequest);

    return getPublicUrl(fileName);
  }

  private String getPublicUrl(String fileName) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, amazonS3.getRegionName(),
        fileName);
  }

  public String updateProfileImage(String email, MultipartFile image) throws IOException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    String existingImageUrl = user.getProfileImage();
    if (existingImageUrl != null) {
      String existingFileName = existingImageUrl.substring(existingImageUrl.lastIndexOf("/") + 1);
      amazonS3.deleteObject(bucket, existingFileName);
    }

    String newFileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(image.getContentType());
    metadata.setContentLength(image.getSize());

    PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, newFileName,
        image.getInputStream(), metadata);
    amazonS3.putObject(putObjectRequest);

    String newImageUrl = getPublicUrl(newFileName);
    user.updateProfileImage(newImageUrl);
    userRepository.save(user);
    return newImageUrl;
  }
}
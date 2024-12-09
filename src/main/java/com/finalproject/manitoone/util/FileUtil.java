package com.finalproject.manitoone.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUtil {

  public void cleanUp(Path path) {
    try {
      Files.delete(path);
    } catch (IOException e) {
      throw new IllegalArgumentException("기존 프로필 이미지를 삭제할 수 없습니다: " + e);
    }
  }

  public void createDir(Path path) {
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      throw new IllegalArgumentException("프로필 이미지를 저장할 디렉토리를 생성할 수 없습니다: " + e);
    }
  }

  public void save(Path path, MultipartFile file) {
    try {
      Files.copy(file.getInputStream(), path,
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalArgumentException("프로필 이미지를 저장하는 중 오류가 발생했습니다.", e);
    }
  }
}

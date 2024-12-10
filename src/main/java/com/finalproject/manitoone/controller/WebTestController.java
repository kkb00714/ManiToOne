package com.finalproject.manitoone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//웹페이지 테스트 컨트롤러
@Controller
public class WebTestController {

  //헤더 네비게이션 메뉴 매핑
  @GetMapping("/fragments/content/{page}")
  public String getContentFragment(@PathVariable String page) {
    return switch (page) {
      case "notification" -> "fragments/content/notification :: notification";
      case "manito" -> "fragments/content/manito :: manito";
      case "mypage" -> "fragments/content/mypage :: mypage";
      default -> "fragments/content/timeline :: timeline";
    };
  }
}

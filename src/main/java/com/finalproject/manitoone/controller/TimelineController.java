package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TimelineController {

  private final TimelineService timelineService;

  @GetMapping("/timeline")
  public Page<PostViewResponseDto> getTimeline(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
      @SessionAttribute("nickname") String nickname,
      @RequestParam(defaultValue = "7") int recentDays) {
    return timelineService.getTimelinePosts(nickname, pageable, recentDays);
  }
}

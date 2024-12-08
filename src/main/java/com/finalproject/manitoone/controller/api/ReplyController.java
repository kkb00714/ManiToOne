package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reply")
public class ReplyController {

  private final ReplyService replyService;
}

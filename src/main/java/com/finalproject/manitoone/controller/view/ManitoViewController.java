package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.dto.manito.ManitoLetterResponseDto;
import com.finalproject.manitoone.service.ManitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/manito")
@RequiredArgsConstructor
public class ManitoViewController {

  private final ManitoService manitoService;

  @GetMapping("/fragments/manito-letter")
  public String getManitoLetterFragment(@RequestParam Long letterId, Model model) {
    ManitoLetterResponseDto letter = manitoService.getLetter(letterId);
    model.addAttribute("letter", letter);
    return "fragments/common/manito-letter :: manito-letter(letter=${letter})";
  }

  @GetMapping
  public String getManitoPage(Model model) {
    return "pages/manito";
  }
}

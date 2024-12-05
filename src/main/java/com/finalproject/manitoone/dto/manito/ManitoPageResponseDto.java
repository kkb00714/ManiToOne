package com.finalproject.manitoone.dto.manito;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManitoPageResponseDto {

  private List<ManitoLetterResponseDto> content;
  private int currentPage;
  private int totalPages;
  private long totalElements;
  private boolean hasNext;
}

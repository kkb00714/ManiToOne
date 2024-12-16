document.addEventListener("DOMContentLoaded", function () {
  const searchInput = document.getElementById("search-input");
  const searchResults = document.getElementById("search-results");
  const searchSection = document.querySelector(".search-section");

  searchInput.addEventListener("keyup", function () {
    // 입력값이 존재하면 스타일 변경
    if (searchInput.value.trim() !== "") {
      searchResults.style.display = "flex"; // 결과창 보여주기
      searchSection.style.borderRadius = "20px 20px 0 0"; // 위쪽 둥글게
    } else {
      // 입력값이 없으면 원래대로
      searchResults.style.display = "none";
      searchSection.style.borderRadius = "20px"; // 전체 둥글게
    }
  });

  // 입력 필드 외의 영역 클릭 시 결과창 숨김
  document.addEventListener("click", function (event) {
    if (!searchInput.contains(event.target) && !searchResults.contains(event.target)) {
      searchResults.style.display = "none";
      searchSection.style.borderRadius = "20px"; // 원래 상태로 복구
    }
  });
});
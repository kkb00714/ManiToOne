document.addEventListener("DOMContentLoaded", function () {
  const aiFeedbackContent = document.getElementById("ai-feedback-text");
  const musicTitle = document.getElementById("music-title");
  const musicLink = document.getElementById("music-link");
  const musicSection = document.getElementById("ai-music-section");
  const suggestionSection = document.getElementById("ai-suggestion-section");

  // 로딩 상태 초기화
  aiFeedbackContent.textContent = "로딩 중...";

  // 서버 API 요청
  fetch("/api/ai-feedback", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })
  .then((response) => {
    if (!response.ok) throw new Error("서버 오류");
    return response.json();
  })
  .then((data) => {
    if (data.content) {
      // AI 피드백 내용에서 줄바꿈 및 역슬래시 제거
      aiFeedbackContent.textContent = data.content;
      // 음악 정보 처리
      if (data.musicTitle != null) {
          musicTitle.textContent = data.musicTitle; // 제목 표시
          if (data.musicLink != null) {
            if (data.musicLink === "추천 노래 없음") {
              musicLink.innerHTML = `${data.musicLink}`;
            } else {
              musicLink.innerHTML = `<a href="${data.musicLink}" target="_blank">${data.musicLink}</a>`;
            }
          }
      } else {
        // 음악 섹션 숨기기
        musicSection.style.display = "none";
      }

      // postId가 있는 경우 클릭 이벤트 추가
      if (data.postId) {
        suggestionSection.style.cursor = "pointer"; // 포인터 커서
        suggestionSection.addEventListener("click", () => {
          window.location.href = `/post/${data.postId}`;
        });
      } else {
        // postId가 없는 경우 클릭 이벤트 제거 및 커서 초기화
        suggestionSection.style.cursor = "default";
        suggestionSection.removeEventListener("click", () => {});
      }
    } else {
      // 데이터가 null인 경우
      aiFeedbackContent.textContent =
          "게시글을 작성하고 AI 피드백을 받아보세요!";
      musicSection.style.display = "none";
    }
  })
  .catch((error) => {
    console.error("에러 발생:", error);
    aiFeedbackContent.textContent = "피드를 불러오는 중 오류가 발생했습니다.";
  });
});
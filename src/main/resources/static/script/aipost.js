document.addEventListener("DOMContentLoaded", function () {
  const aiFeedbackContent = document.getElementById("ai-feedback-text");
  const musicTitle = document.getElementById("music-title");
  const musicLink = document.getElementById("music-link");
  const musicSection = document.getElementById("ai-music-section");

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
      const cleanedContent = data.content.replace(/\\n|\\/g, " ").trim();
      aiFeedbackContent.textContent = cleanedContent;

      // 음악 정보 처리
      if (data.musicContent) {
        const musicParts = data.musicContent.match(/\*\*(.*?)\*\*.*\((.*?)\)/);
        if (musicParts) {
          const artistSong = musicParts[1]; // 가수와 노래 제목
          const link = musicParts[2]; // 링크만 추출

          musicTitle.textContent = artistSong; // 제목 표시
          musicLink.innerHTML = `<a href="${link}" target="_blank">${link}</a>`;
        }
      } else {
        // 음악 섹션 숨기기
        musicSection.style.display = "none";
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
document.addEventListener("DOMContentLoaded", () => {
  const userEmail = document.querySelector('meta[name="email"]').getAttribute("content");

  if (userEmail) {
    // WebSocket 연결 설정
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const host = window.location.host; // ex: localhost:8080 또는 your-domain.com
    const webSocket = new WebSocket(`${protocol}//${host}/ws-alarm`);

    webSocket.onopen = () => {
      webSocket.send(userEmail); // 서버로 사용자 이메일 전송
    };

    webSocket.onmessage = (e) => {
      const notiImage = document.querySelector(".noti-image");

      if (notiImage) {
        localStorage.setItem("isRead", 'true');
        notiImage.src = "/images/icons/UI-notification2-on.png";
      }
    };

    webSocket.onclose = () => {
    };

    webSocket.onerror = (error) => {
    };
  }
});
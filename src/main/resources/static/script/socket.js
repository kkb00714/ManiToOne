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
      const notificationSection = document.querySelector(".notification-section");

      if (notiImage) {
        localStorage.setItem("isRead", 'true');
        notiImage.src = "/images/icons/UI-notification2-on.png";
      }

      // 알림 페이지라면 새로운 알림 추가
      if (notificationSection) {
        const notificationData = JSON.parse(e.data); // 메시지가 JSON 형식이라 가정

        // 새로운 알림 항목 생성
        const newNotification = document.createElement("div");
        newNotification.classList.add("notification-container");
        newNotification.setAttribute("data-type", notificationData.type);
        newNotification.setAttribute("data-id", notificationData.relatedObjectId);
        newNotification.setAttribute("data-nickname", notificationData.senderUser.nickname || "");

        // 알림 내용 구성
        newNotification.innerHTML = `
          <img class="user-photo" 
               src="${notificationData.senderUser.profileImage}" 
               alt="user icon"/>
          <div class="notification-content">
              <span class="notification-description">
                  ${notificationData.content}
              </span>
              <span class="passed-time">
                  ${notificationData.timeDifference}
              </span>
          </div>
        `;

        // 알림 섹션의 맨 위에 새로운 알림 추가
        notificationSection.prepend(newNotification);

        // **새로운 알림에 클릭 이벤트 리스너 추가**
        newNotification.addEventListener('click', () => {
          const type = newNotification.getAttribute('data-type');
          const relatedObjectId = newNotification.getAttribute('data-id');
          const nickname = newNotification.getAttribute('data-nickname');

          handleNotificationClick(type, relatedObjectId, nickname);
        });
      }
    };

    webSocket.onclose = () => {
    };

    webSocket.onerror = (error) => {
    };
  }
});
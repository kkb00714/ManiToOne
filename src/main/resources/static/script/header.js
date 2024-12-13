document.addEventListener("DOMContentLoaded", () => {
  const isRead = localStorage.getItem("isRead");
  const userEmail = document.querySelector('meta[name="email"]').getAttribute(
      "content");
  const notiImage = document.querySelector(".noti-image");

  if (notiImage) {
    if (isRead === null) {
      if (userEmail !== "") {
        // 서버 통신
        fetch("/notifications/status")
        .then(response => response.json())
        .then(data => {
          const hasUnread = data;
          localStorage.setItem("isRead", hasUnread);
          notiImage.src = hasUnread
              ? "/images/icons/UI-notification2-on.png"
              : "/images/icons/UI-notification2.png";
        })
        .catch(error => {
          notiImage.src = "/images/icons/UI-notification2.png";
        });
      } else {
        notiImage.src = "/images/icons/UI-notification2.png";
      }
    } else {
      if (isRead === 'true') {
        notiImage.src = "/images/icons/UI-notification2-on.png";
      } else {
        notiImage.src = "/images/icons/UI-notification2.png";
      }
    }
  } else {
    console.error("알림 아이콘을 찾을 수 없습니다.");
  }
});
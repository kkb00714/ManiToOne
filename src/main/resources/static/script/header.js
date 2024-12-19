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

document.addEventListener('DOMContentLoaded', function () {
  const moreBtn = document.getElementById('more-options-btn');
  const tooltipMenu = document.getElementById('tooltip-menu');
  const logoutBtn = document.getElementById('logout-btn');
  const cancelAccountBtn = document.getElementById('cancel-account-btn');

  // 툴팁 메뉴 토글
  moreBtn.addEventListener('click', function () {
    tooltipMenu.classList.toggle('hidden');
  });

  // 클릭 외 다른 곳 클릭 시 툴팁 닫기
  document.addEventListener('click', function (event) {
    if (!moreBtn.contains(event.target) && !tooltipMenu.contains(
        event.target)) {
      tooltipMenu.classList.add('hidden');
    }
  });

  // 로그아웃 버튼 클릭 이벤트
  logoutBtn.addEventListener("click", function () {
    const userConfirmed = confirm("정말 로그아웃 하시겠습니까?");
    if (userConfirmed === true) {
      fetch("/logout", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded", // Spring Security 기본 설정
        },
      })
      .then((response) => {
        if (response.ok) {
          alert("로그아웃에 성공했습니다. 바이바이!");
          window.location.href = "/login";
        } else {
          console.error("로그아웃 실패:", response.status, response.statusText);
          alert("로그아웃 처리에 실패했습니다. 서버에 문제가 발생했습니다.");
        }
      })
      .catch((error) => {
        console.error("로그아웃 요청 중 오류 발생:", error);
        alert("네트워크 오류로 로그아웃을 처리할 수 없습니다.");
      });
    }
  });

  // 회원탈퇴 버튼 클릭 이벤트
  cancelAccountBtn.addEventListener("click", function () {
    const userConfirmed = confirm("정말 회원 탈퇴를 진행하시겠습니까?");
    if (userConfirmed) {
      window.location.href = "/cancel-account";
    }
  });
});
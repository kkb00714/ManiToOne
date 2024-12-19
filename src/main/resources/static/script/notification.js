function handleNotificationClick(notiType, id) {
  // ID로 알림 데이터 가져오기
  fetch(`/api/notification/${id}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  })
  .then((response) => {
    if (!response.ok) throw new Error('Failed to fetch notification data');
    return response.json();
  })
  .then((notification) => {
    const { type, relatedObjectId, senderUser } = notification;

    // 타입에 따라 URL 결정
    switch (type) {
      case 'POST_REPLY':
      case 'POST_RE_REPLY':
      case 'LIKE_CLOVER':
        // 게시글 상세 페이지로 이동
        url = `/post/${relatedObjectId}`;
        break;
      case 'FOLLOW':
        // FOLLOW 타입이면 senderUser의 닉네임 사용
        if (senderUser && senderUser.nickname) {
          url = `/profile/${senderUser.nickname}`;
        } else {
          url = '/';
        }
        break;
      case 'RECEIVE_MANITO':
      case 'MANITO_LETTER':
      case 'MANITO_ANSWER_LETTER':
        url = `/manito?letterId=${relatedObjectId}&tab=received`;
        break;
      default:
        url = '/';
    }

    // URL로 이동
    if (url) {
      window.location.href = url;
    }
  })
  .catch((error) => {
    console.error('Error fetching notification data:', error);
    alert('알림 데이터를 불러오는 중 오류가 발생했습니다.');
  });
}

function readAllNotification() {
  // 알림 읽음 처리
  fetch('/api/notification', {
    method: 'PUT',
  })
  .then(response => {
    if (response.ok) {
      localStorage.setItem('isRead', 'false');
      const notiImage = document.querySelector('.noti-image');
      if (notiImage) {
        notiImage.src = '/images/icons/UI-notification2.png';
      }
    } else {
      console.error('알림 읽음 처리 실패');
    }
  })
  .catch(error => {
    console.error('서버 통신 오류:', error);
  });
}

document.addEventListener('DOMContentLoaded', () => {
  // 알림 모두 읽음 처리
  readAllNotification();
  // 모든 notification-container에 이벤트 리스너 추가
  const notificationContainers = document.querySelectorAll('.notification-container');
  notificationContainers.forEach(container => {
    container.addEventListener('click', () => {
      const type = container.getAttribute('data-type');
      const id = container.getAttribute('data-id');

      handleNotificationClick(type, id);
    });
  });
});
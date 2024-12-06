function handleNotificationClick(notiType, relatedObjectId, nickname) {
  let url;
  switch (notiType) {
    case 'POST_REPLY':
    case 'POST_RE_REPLY':
    case 'LIKE_CLOVER':
      // 게시글 상세 페이지로 이동
      url = `/`;
      break;
    case 'FOLLOW':
      url = `/profile/${nickname}`;
      break;
    case 'RECEIVE_MANITO':
    case 'MANITO_COMMENT':
    case 'MANITO_THANK_COMMENT':
      // 마니또 페이지 연결
      url = `/`;
      break;
    default:
      url = '/notifications';
  }
  window.location.href = url; // 해당 URL로 이동
}

document.addEventListener('DOMContentLoaded', () => {
  // 모든 notification-container에 이벤트 리스너 추가
  const notificationContainers = document.querySelectorAll('.notification-container');
  notificationContainers.forEach(container => {
    container.addEventListener('click', () => {
      const type = container.getAttribute('data-type');
      const relatedObjectId = container.getAttribute('data-id');
      const nickname = container.getAttribute('data-nickname');

      handleNotificationClick(type, relatedObjectId, nickname);
    });
  });
});
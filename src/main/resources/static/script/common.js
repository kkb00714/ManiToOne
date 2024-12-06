class BaseModal {
  constructor(modalId, backgroundId, openBtnId, closeBtnId) {
    this.modal = document.getElementById(modalId);
    this.background = document.getElementById(backgroundId);
    this.openBtn = document.getElementById(openBtnId);
    this.closeBtn = document.getElementById(closeBtnId);

    this.initializeEventListeners();
    this.initializeTextareas();

    if (modalId === 'manitoLetterModalContainer') {
      this.initializeSendConfirmation();
    }
  }

  initializeEventListeners() {
    this.openBtn.onclick = () => this.open();
    this.closeBtn.onclick = () => this.close();
  }

  initializeTextareas() {
    if (this.modal) {
      const textareas = this.modal.getElementsByTagName('textarea');
      Array.from(textareas).forEach(textarea => {
        this.adjustTextareaHeight(textarea);

        textarea.addEventListener('input', () => {
          this.adjustTextareaHeight(textarea);
        });
      });
    }
  }

  adjustTextareaHeight(element) {
    element.style.height = 'auto';
    element.style.height = (element.scrollHeight) + 'px';
  }

  // 스크롤 잠금 메서드 추가
  lockScroll() {
    document.body.style.overflow = 'hidden';
    document.body.style.paddingRight = this.getScrollbarWidth() + 'px'; // 스크롤바 너비만큼 패딩 추가
  }

  // 스크롤 잠금 해제 메서드 추가
  unlockScroll() {
    document.body.style.overflow = '';
    document.body.style.paddingRight = ''; // 패딩 제거
  }

  // 스크롤바 너비 계산 메서드 추가
  getScrollbarWidth() {
    // 스크롤바 너비 계산
    const outer = document.createElement('div');
    outer.style.visibility = 'hidden';
    outer.style.overflow = 'scroll';
    document.body.appendChild(outer);

    const inner = document.createElement('div');
    outer.appendChild(inner);

    const scrollbarWidth = outer.offsetWidth - inner.offsetWidth;
    outer.parentNode.removeChild(outer);

    return scrollbarWidth;
  }

  open() {
    [this.modal, this.background].forEach((el) => (el.style.display = "block"));
    this.lockScroll(); // 모달 열 때 스크롤 잠금
  }

  close() {
    [this.modal, this.background].forEach((el) => (el.style.display = "none"));
    this.unlockScroll(); // 모달 닫을 때 스크롤 잠금 해제
  }

  showWarning(message) {
    const warningPopup = document.getElementById('warningPopup');
    const warningMessage = document.getElementById('warningMessage');
    const warningConfirmBtn = document.getElementById('warningConfirmBtn');

    warningMessage.textContent = message;
    warningPopup.style.display = 'block';

    warningConfirmBtn.onclick = () => {
      warningPopup.style.display = 'none';
    };
  }

  isValidYoutubeUrl(url) {
    if (!url) return true; // URL이 비어있으면 통과 (필수 아님)

    const youtubeRegex = /^(https?:\/\/)?(www\.)?(youtube\.com|youtu\.be)\/.+/;
    return youtubeRegex.test(url);
  }

  validateForm() {
    const letterText = this.modal.querySelector('#manito-letter-text-input');
    const musicUrl = this.modal.querySelector('#music-link-input');

    // 편지 내용 검사
    if (!letterText.value.trim()) {
      this.showWarning('편지 내용을 작성해주세요.');
      letterText.focus();
      return false;
    }

    // YouTube URL 검사 (입력된 경우에만)
    if (musicUrl.value.trim() && !this.isValidYoutubeUrl(musicUrl.value.trim())) {
      this.showWarning('Youtube url을 입력해주세요.');
      musicUrl.focus();
      return false;
    }

    return true;
  }

  // 폼 초기화 메서드 추가
  resetForm() {
    if (this.modal) {
      // textarea 초기화
      const textareas = this.modal.getElementsByTagName('textarea');
      Array.from(textareas).forEach(textarea => {
        textarea.value = '';
        this.adjustTextareaHeight(textarea); // 높이도 초기화
      });

      // input 초기화
      const inputs = this.modal.getElementsByTagName('input');
      Array.from(inputs).forEach(input => {
        input.value = '';
      });
    }
  }

  initializeSendConfirmation() {
    const sendButton = this.modal.querySelector('.send-letter-button');
    const confirmationPopup = document.getElementById('sendConfirmationPopup');
    const successPopup = document.getElementById('sendSuccessPopup');
    const confirmBtn = document.getElementById('confirmSendBtn');
    const cancelBtn = document.getElementById('cancelSendBtn');
    const successConfirmBtn = document.getElementById('successConfirmBtn');

    if (sendButton && confirmationPopup && confirmBtn && cancelBtn) {
      sendButton.addEventListener('click', (e) => {
        e.preventDefault();
        if (this.validateForm()) {
          confirmationPopup.style.display = 'block';
        }
      });

      confirmBtn.addEventListener('click', (e) => {
        e.preventDefault();
        confirmationPopup.style.display = 'none';
        successPopup.style.display = 'block';

        // 여기에 실제 전송 로직 추가
        console.log('편지 전송 완료!');

        // 폼 초기화 추가
        this.resetForm();
      });

      cancelBtn.addEventListener('click', (e) => {
        e.preventDefault();
        confirmationPopup.style.display = 'none';
      });

      successConfirmBtn.addEventListener('click', (e) => {
        e.preventDefault();
        successPopup.style.display = 'none';
        this.close();
      });
    }
  }
}

class PostFormModal extends BaseModal {
  constructor() {
    super(
        "newPostFormModalContainer",
        "newPostFormModalBackground",
        "openPostFormModalBtn",
        "closePostFormModalBtn"
    );
  }
}

class SendManitoLetterModal extends BaseModal {
  constructor() {
    super(
        "manitoLetterModalContainer",
        "manitoLetterModalBackground",
        "openManitoLetterModalBtn",
        "closeManitoLetterModalBtn"
    );
  }
}

class SendManitoLetterReplyModal extends BaseModal {
  constructor() {
    super(
        "manitoLetterReplyModalContainer",
        "manitoLetterReplyModalBackground",
        "openManitoLetterReplyModalBtn",
        "closeManitoLetterReplyModalBtn"
    );
  }
}

class ProfileUpdateModal extends BaseModal {
  constructor() {
    super(
        "profileUpdateModalContainer",
        "profileUpdateModalBackground",
        "openProfileUpdateBtn",
        "closeProfileUpdateBtn"
    );
  }
}

function initializeModals() {
  try {
    if (document.getElementById("newPostFormModalContainer")) {
      new PostFormModal();
    }
    if (document.getElementById("manitoLetterModalContainer")) {
      new SendManitoLetterModal();
    }
    if (document.getElementById("manitoLetterReplyModalContainer")) {
      new SendManitoLetterReplyModal();
    }
    if (document.getElementById("profileUpdateModalContainer")) {
      new ProfileUpdateModal();
    }
  } catch (error) {
    console.error('모달 초기화 중 오류 발생:', error);
  }
}

// 모든 textarea 자동 리사이즈를 위한 전역 함수 추가
function initializeAllTextareas() {
  const allTextareas = document.getElementsByTagName('textarea');
  Array.from(allTextareas).forEach(textarea => {
    const adjustHeight = () => {
      textarea.style.height = 'auto';
      textarea.style.height = (textarea.scrollHeight) + 'px';
    };

    // 초기 높이 설정
    adjustHeight();

    // 이벤트 리스너 추가
    textarea.addEventListener('input', adjustHeight);
  });
}

function loadContent(page) {
  const middleSection = document.getElementById('middleSection');
  middleSection.innerHTML = '<div class="loading">Loading...</div>';

  let url = '';
  if (page === 'notification') {
    return;
  } else {
    url = '/fragments/content/' + page;
  }

  fetch(url)
  .then(response => {
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    return response.text();
  })
  .then(html => {
    middleSection.innerHTML = html;
    history.pushState({page: page}, '', `/${page}`);
    initializeModals();
    initializeAllTextareas(); // 새로운 콘텐츠에 대한 textarea 초기화 추가
  })
  .catch(error => {
    console.error('Error:', error);
    middleSection.innerHTML = '<div class="error">Failed to load content</div>';
  });
}

document.addEventListener('DOMContentLoaded', function () {
  const navButtons = document.querySelectorAll('.UI-icon-list button');

  navButtons.forEach(button => {
    button.addEventListener('click', function () {
      const buttonType = this.querySelector('img').alt;

      switch (buttonType) {
        case 'home':
          loadContent('timeline');
          break;
        case 'notification':
          loadContent('notification');
          break;
        case 'manito':
          loadContent('manito');
          break;
        case 'user-profile':
          loadContent('mypage');
          break;
      }
    });
  });

  const logoLink = document.querySelector('.home-link');
  if (logoLink) {
    logoLink.addEventListener('click', function (e) {
      e.preventDefault();
      loadContent('timeline');
    });
  }

  initializeModals();
  initializeAllTextareas(); // 페이지 로드 시 모든 textarea 초기화
});

function toggleManito(element, type) {
  const img = element.querySelector('img');
  const isChecked = img.src.includes('icon-check.png');

  if (isChecked) {
    img.src = img.getAttribute('data-unchecked-src').replace('@{', '').replace('}', '');
    element.style.opacity = '0.3';
  } else {
    img.src = img.getAttribute('data-checked-src').replace('@{', '').replace('}', '');
    element.style.opacity = '1';
  }

}
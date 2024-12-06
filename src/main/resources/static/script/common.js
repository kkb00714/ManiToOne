class BaseModal {
  constructor(modalId, backgroundId, openBtnId, closeBtnId) {
    this.modal = document.getElementById(modalId);
    this.background = document.getElementById(backgroundId);
    this.openBtn = document.getElementById(openBtnId);
    this.closeBtn = document.getElementById(closeBtnId);

    this.initializeEventListeners();
    this.initializeTextareas();

    if (modalId === 'manitoLetterModalContainer' ||
        modalId === 'manitoLetterReplyModalContainer') {
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

  lockScroll() {
    document.body.style.overflow = 'hidden';
    document.body.style.paddingRight = this.getScrollbarWidth() + 'px';
  }

  unlockScroll() {
    document.body.style.overflow = '';
    document.body.style.paddingRight = '';
  }

  getScrollbarWidth() {
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
    this.lockScroll();
  }

  close() {
    [this.modal, this.background].forEach((el) => (el.style.display = "none"));
    this.unlockScroll();
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
    if (!url) {
      return true;
    }

    const youtubeRegex = /^(https?:\/\/)?(www\.)?(youtube\.com|youtu\.be)\/.+/;
    return youtubeRegex.test(url);
  }

  validateForm() {
    const letterText = this.modal.querySelector('textarea');

    if (!letterText.value.trim()) {
      const message = this.modal.id === 'manitoLetterReplyModalContainer'
          ? '답장 내용을 작성해주세요.'
          : '편지 내용을 작성해주세요.';
      this.showWarning(message);
      letterText.focus();
      return false;
    }

    if (this.modal.id === 'manitoLetterModalContainer') {
      const musicUrl = this.modal.querySelector('#music-link-input');
      if (musicUrl && musicUrl.value.trim() && !this.isValidYoutubeUrl(musicUrl.value.trim())) {
        this.showWarning('Youtube url을 입력해주세요.');
        musicUrl.focus();
        return false;
      }
    }

    return true;
  }

  resetForm() {
    if (this.modal) {
      const textareas = this.modal.getElementsByTagName('textarea');
      Array.from(textareas).forEach(textarea => {
        textarea.value = '';
        this.adjustTextareaHeight(textarea);
      });

      const inputs = this.modal.getElementsByTagName('input');
      Array.from(inputs).forEach(input => {
        input.value = '';
      });
    }
  }

  initializeSendConfirmation() {
    const sendButton = this.modal.querySelector('.send-letter-button, .send-letter-reply-button');

    const isLetterModal = this.modal.id === 'manitoLetterModalContainer';
    const confirmationPopup = document.getElementById(isLetterModal ? 'letterConfirmationPopup' : 'sendConfirmationPopup');
    const successPopup = document.getElementById(isLetterModal ? 'letterSuccessPopup' : 'sendSuccessPopup');
    const confirmBtn = document.getElementById(isLetterModal ? 'letterConfirmSendBtn' : 'confirmSendBtn');
    const cancelBtn = document.getElementById(isLetterModal ? 'letterCancelSendBtn' : 'cancelSendBtn');
    const successConfirmBtn = document.getElementById(isLetterModal ? 'letterSuccessConfirmBtn' : 'successConfirmBtn');

    if (sendButton && confirmationPopup && confirmBtn && cancelBtn) {
      sendButton.replaceWith(sendButton.cloneNode(true));
      const newSendButton = this.modal.querySelector('.send-letter-button, .send-letter-reply-button');

      newSendButton.addEventListener('click', (e) => {
        e.preventDefault();
        if (this.validateForm()) {
          confirmationPopup.style.display = 'block';
        }
      });

      confirmBtn.onclick = (e) => {
        e.preventDefault();
        confirmationPopup.style.display = 'none';
        successPopup.style.display = 'block';
        console.log('전송 완료!');
      };

      cancelBtn.onclick = (e) => {
        e.preventDefault();
        confirmationPopup.style.display = 'none';
      };

      successConfirmBtn.onclick = (e) => {
        e.preventDefault();
        successPopup.style.display = 'none';
        this.resetForm();
        this.close();
      };
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

function initializeAllTextareas() {
  const allTextareas = document.getElementsByTagName('textarea');
  Array.from(allTextareas).forEach(textarea => {
    const adjustHeight = () => {
      textarea.style.height = 'auto';
      textarea.style.height = (textarea.scrollHeight) + 'px';
    };

    adjustHeight();

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
    initializeAllTextareas();
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
  initializeAllTextareas();
});

function toggleManito(element, type) {
  const img = element.querySelector('img');
  const isChecked = img.src.includes('icon-check.png');

  if (isChecked) {
    img.src = img.getAttribute('data-unchecked-src').replace('@{', '').replace(
        '}', '');
    element.style.opacity = '0.3';
  } else {
    img.src = img.getAttribute('data-checked-src').replace('@{', '').replace(
        '}', '');
    element.style.opacity = '1';
  }

}
//common.js

class BaseModal {
  constructor(modalId, backgroundId, openBtnId, closeBtnId) {
    this.modal = document.getElementById(modalId);
    this.background = document.getElementById(backgroundId);
    this.openBtn = document.getElementById(openBtnId);
    this.closeBtn = document.getElementById(closeBtnId);

    if (this.modal && this.background) {
      this.initializeEventListeners();
      this.initializeTextareas();
    }
  }

  initializeEventListeners() {
    if (this.openBtn) {
      this.openBtn.onclick = () => this.open();
    }
    if (this.closeBtn) {
      this.closeBtn.onclick = () => this.close();
    }
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
    if (this.modal && this.background) {
      [this.modal, this.background].forEach(
          (el) => (el.style.display = "block"));
      this.lockScroll();
    }
  }

  close() {
    if (this.modal && this.background) {
      [this.modal, this.background].forEach(
          (el) => (el.style.display = "none"));
      this.unlockScroll();
    }
  }

  showWarning(message) {
    const warningPopup = document.getElementById('warningPopup');
    const warningMessage = document.getElementById('warningMessage');
    const warningConfirmBtn = document.getElementById('warningConfirmBtn');  // 변수명 수정

    if (warningPopup && warningMessage && warningConfirmBtn) {  // 변수명 수정
      warningMessage.textContent = message;
      warningPopup.style.display = 'block';

      // 기존 이벤트 리스너 제거를 위한 복제
      const newConfirmButton = warningConfirmBtn.cloneNode(true);
      warningConfirmBtn.parentNode.replaceChild(newConfirmButton,
          warningConfirmBtn);

      // 새로운 이벤트 리스너 추가
      newConfirmButton.addEventListener('click', () => {
        warningPopup.style.display = 'none';
      });
    }
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

      const toggleElements = this.modal.querySelectorAll('[data-checked-src]');
      Array.from(toggleElements).forEach(element => {
        const img = element.querySelector('img');
        if (img) {
          img.src = img.getAttribute('data-unchecked-src').replace('@{',
              '').replace('}', '');
          element.style.opacity = '0.3';
        }
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

const CommonUtils = {
  initializeAllTextareas() {
    const allTextareas = document.getElementsByTagName('textarea');
    Array.from(allTextareas).forEach(textarea => {
      const adjustHeight = () => {
        textarea.style.height = 'auto';
        textarea.style.height = (textarea.scrollHeight) + 'px';
      };

      adjustHeight();
      textarea.addEventListener('input', adjustHeight);
    });
  },

  initializePageModals() {
    try {
      if (document.getElementById("newPostFormModalContainer")) {
        new PostFormModal();
      }
      if (document.getElementById("profileUpdateModalContainer")) {
        new ProfileUpdateModal();
      }
    } catch (error) {
      console.error('모달 초기화 중 오류 발생:', error);
    }
  },

  toggleElement(element, type) {
    const img = element.querySelector('img');
    if (!img) {
      return;
    }

    const isChecked = img.src.includes('icon-check.png');

    if (isChecked) {
      img.src = img.getAttribute('data-unchecked-src').replace('@{',
          '').replace('}', '');
      element.style.opacity = '0.3';
    } else {
      img.src = img.getAttribute('data-checked-src').replace('@{', '').replace(
          '}', '');
      element.style.opacity = '1';
    }
  },

  initializeRightSectionManito() {
    const elements = {
      receivedList: document.querySelector(
          '.manito-letter-section .received-letter ul'),
      sentList: document.querySelector(
          '.manito-letter-section .sent-letter ul'),
      receivedLink: document.querySelector(
          '.manito-letter-section .received-letter h3 a'),
      sentLink: document.querySelector(
          '.manito-letter-section .sent-letter h3 a')
    };

    if (!elements.receivedList || !elements.sentList) {
      return;
    }

    this.loadRecentLetters(elements);
    this.initializeLetterEventListeners(elements);
  },

  async loadRecentLetters(elements) {
    try {
      const userNickname = document.querySelector(
          'meta[name="user-nickname"]')?.content;
      if (!userNickname) {
        return;
      }

      this.showLoadingState(elements.receivedList);
      this.showLoadingState(elements.sentList);

      const receivedResponse = await fetch(
          `/api/receivemanito/${userNickname}?page=0&size=3`);
      const receivedData = await receivedResponse.json();

      const sentResponse = await fetch(
          `/api/sendmanito/${userNickname}?page=0&size=3`);
      const sentData = await sentResponse.json();

      this.updateLetterList(elements.receivedList, receivedData.content, true);
      this.updateLetterList(elements.sentList, sentData.content, false);

    } catch (error) {
      console.error('Error loading recent letters:', error);
      this.updateLetterList(elements.receivedList, [], true);
      this.updateLetterList(elements.sentList, [], false);
    }
  },

  updateLetterList(container, letters, isReceived) {
    container.innerHTML = '';

    if (!letters || letters.length === 0) {
      const emptyMessage = document.createElement('li');
      emptyMessage.className = 'empty-message';
      emptyMessage.textContent = '편지함이 비어 있습니다';
      emptyMessage.style.color = '#888';
      emptyMessage.style.textAlign = 'center';
      emptyMessage.style.padding = '10px 0';
      container.appendChild(emptyMessage);
      return;
    }

    letters.forEach(letter => {
      const content = letter.letterContent || '';
      const truncatedContent = content.substring(0, 20);

      const li = document.createElement('li');
      li.textContent = truncatedContent;
      li.dataset.letterId = letter.manitoLetterId;
      li.style.cursor = 'pointer';
      container.appendChild(li);
    });
  },

  showLoadingState(container) {
    container.innerHTML = '';
    const loadingMessage = document.createElement('li');
    loadingMessage.className = 'loading-message';
    loadingMessage.textContent = '편지 로딩 중...';
    loadingMessage.style.color = '#888';
    loadingMessage.style.textAlign = 'center';
    loadingMessage.style.padding = '10px 0';
    container.appendChild(loadingMessage);
  },

  initializeLetterEventListeners(elements) {

    elements.receivedList?.addEventListener('click', (e) => {
      const letterId = e.target.dataset.letterId;
      if (letterId) {
        e.preventDefault();
        window.location.href = `/manito?tab=received&letterId=${letterId}`;
      }
    });

    elements.sentList?.addEventListener('click', (e) => {
      const letterId = e.target.dataset.letterId;
      if (letterId) {
        e.preventDefault();
        window.location.href = `/manito?tab=sent&letterId=${letterId}`;
      }
    });

    elements.receivedLink?.addEventListener('click', (e) => {
      window.location.href = '/manito?tab=received';
    });

    elements.sentLink?.addEventListener('click', (e) => {
      window.location.href = '/manito?tab=sent';
    });
  },

};

document.addEventListener('DOMContentLoaded', () => {
  CommonUtils.initializePageModals();
  CommonUtils.initializeAllTextareas();
  CommonUtils.initializeRightSectionManito();

  window.toggleManito = function (element, type) {
    CommonUtils.toggleElement(element, type);
  };
});
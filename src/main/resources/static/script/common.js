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

    this.initializeCloseHandlers();
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
    CommonUtils.showWarningMessage(message);
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
  initializeCloseHandlers() {
    // ESC 키 눌렀을 때 닫기
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && this.modal?.style.display === 'block') {
        this.close();
      }
    });

    // 모달 바깥 클릭시 닫기
    if (this.background) {
      this.background.addEventListener('click', (e) => {
        if (e.target === this.background) {
          this.close();
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

const ContentValidator = {
  validateContentQuality(text) {
    const trimmedText = text.trim();

    // 연속된 자음/모음 패턴 검사
    const consonantPattern = /[ㄱ-ㅎ]{3,}/g;
    const vowelPattern = /[ㅏ-ㅣ]{3,}/g;

    // 같은 글자 반복 패턴 검사 (한글)
    const repeatedCharPattern = /([\uAC00-\uD7AF])\1{9,}/;

    // 연속된 ㅋ, ㅎ, ㅠ, ㅜ 등의 패턴
    const repeatedConsonantPattern = /([ㅋㅎㅠㅜ])\1{4,}/g;

    const consonantMatches = text.match(consonantPattern) || [];
    const vowelMatches = text.match(vowelPattern) || [];
    const repeatedConsMatches = text.match(repeatedConsonantPattern) || [];

    // 반복 문자의 총 길이 계산
    const totalRepeatedLength =
        consonantMatches.join('').length +
        vowelMatches.join('').length +
        repeatedConsMatches.join('').length;

    // 반복 문자가 전체의 50% 이상이거나 같은 글자가 10회 이상 반복되면 false 반환
    if (totalRepeatedLength > trimmedText.length / 2 || repeatedCharPattern.test(trimmedText)) {
      return {
        isValid: false,
        message: '내용에 반복되는 문구가 많은 것 같아요. 정성을 담아 작성해주세요.'
      };
    }

    return {
      isValid: true,
      message: ''
    };
  }
};

const CommonUtils = {
  ContentValidator,

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

  toggleElement(element) {
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
      const sessionResponse = await fetch('/api/session-info');
      if (!sessionResponse.ok) {
        return;
      }
      const sessionData = await sessionResponse.json();
      const userNickname = sessionData.nickname;

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

      this.updateLetterList(elements.receivedList, receivedData.content);
      this.updateLetterList(elements.sentList, sentData.content);

    } catch (error) {
      console.error('Error loading recent letters:', error);
      this.updateLetterList(elements.receivedList, []);
      this.updateLetterList(elements.sentList, []);
    }
  },

  updateLetterList(container, letters) {
    container.innerHTML = '';

    if (!letters || letters.length === 0) {
      const emptyMessage = document.createElement('li');
      emptyMessage.className = 'empty-message';
      emptyMessage.textContent = '편지함이 비어 있습니다';
      emptyMessage.style.color = '#888';
      emptyMessage.style.textAlign = 'left';
      emptyMessage.style.padding = '10px 0';
      container.appendChild(emptyMessage);
      return;
    }

    // 신고되지 않은 편지만 필터링
    const validLetters = letters.filter(letter => !letter.report);

    if (validLetters.length === 0) {
      const emptyMessage = document.createElement('li');
      emptyMessage.className = 'empty-message';
      emptyMessage.textContent = '편지함이 비어 있습니다';
      emptyMessage.style.color = '#888';
      emptyMessage.style.textAlign = 'left';
      emptyMessage.style.padding = '10px 0';
      container.appendChild(emptyMessage);
      return;
    }

    validLetters.forEach(letter => {
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
    loadingMessage.style.textAlign = 'left';
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

    elements.receivedLink?.addEventListener('click', () => {
      window.location.href = '/manito?tab=received';
    });

    elements.sentLink?.addEventListener('click', () => {
      window.location.href = '/manito?tab=sent';
    });
  },

  showWarningMessage(message) {
    const warningPopup = document.getElementById('warningPopup');
    const warningMessage = document.getElementById('warningMessage');
    const warningConfirmBtn = document.getElementById('warningConfirmBtn');

    if (warningPopup && warningMessage && warningConfirmBtn) {
      warningMessage.textContent = message;
      warningPopup.style.display = 'block';

      const newConfirmBtn = warningConfirmBtn.cloneNode(true);
      // 오타 수정: newConfirmButton -> newConfirmBtn
      newConfirmBtn.addEventListener('click', () => {
        warningPopup.style.display = 'none';
      });
      warningConfirmBtn.parentNode.replaceChild(newConfirmBtn, warningConfirmBtn);
    }
  }
};

document.addEventListener('DOMContentLoaded', () => {
  CommonUtils.initializePageModals();
  CommonUtils.initializeAllTextareas();
  CommonUtils.initializeRightSectionManito();

  window.toggleManito = function (element, type) {
    CommonUtils.toggleElement(element, type);
  };
});
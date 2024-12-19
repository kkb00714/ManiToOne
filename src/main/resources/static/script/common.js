//common.js

window.toggleAI = function(element) {
  CommonUtils.toggleElement(element);
};

window.toggleManito = function (element, type) {
  CommonUtils.toggleElement(element, type);
};

document.addEventListener('DOMContentLoaded', () => {
  CommonUtils.initializePageModals();
  CommonUtils.initializeAllTextareas();
  CommonUtils.initializeCharacterCounters();
  CommonUtils.initializeRightSectionManito();
});

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

class CharacterCounter {
  constructor(textarea, countDisplay, maxLength) {
    this.textarea = textarea;
    this.countDisplay = countDisplay;
    this.maxLength = maxLength;
    this.init();
  }

  init() {
    this.updateCount();
    this.textarea.addEventListener('input', () => this.updateCount());
  }

  updateCount() {
    const currentLength = this.textarea.value.length;
    this.countDisplay.textContent = `${currentLength}/${this.maxLength}`;
  }
}

const ContentValidator = {
  validateContentQuality(text) {
    const trimmedText = text.trim();

    // 1. 연속된 같은 문자 패턴 (한글, 영문, 특수문자 모두 포함)
    const sameCharPattern = /(.)\1{29,}/;
    const matches = trimmedText.match(sameCharPattern);

    // 2. 연속된 자음/모음/특정 문자 패턴
    const repeatedPattern = /([\u3131-\u314E\u314F-\u3163ㅋㅎㅠㅜzZ])\1{29,}/g;
    const repeatedMatches = trimmedText.match(repeatedPattern) || [];

    // 3. 문장 반복 패턴 검사
    const repeatingPhrases = this.findSignificantRepeats(trimmedText);
    const totalLength = trimmedText.length;
    let totalRepeatedLength = repeatingPhrases.reduce((sum, phrase) =>
        sum + (phrase.text.length * (phrase.count - 1)), 0);

    // 기존 검사 조건
    if (matches || repeatedMatches.length > 0) {
      return {
        isValid: false,
        message: '내용에 무의미하게 반복되는 글자가 많은 것 같아요. 정성을 담아 작성하면 편지를 받는 사람이 기쁠 거예요.'
      };
    }

    // 공백 문자가 과도하게 많은 경우 체크
    const nonWhitespaceLength = trimmedText.replace(/\s/g, '').length;
    if (nonWhitespaceLength < trimmedText.length * 0.6) { // 40% 이상이 공백인 경우
      return {
        isValid: false,
        message: '내용에 공백이 너무 많아요. 당신의 메세지를 조금 더 담아보면 좋을 것 같아요.'
      };
    }

    // 유의미한 반복이 전체 텍스트의 50% 이상인 경우
    if (totalRepeatedLength / totalLength > 0.5 && repeatingPhrases.some(phrase =>
        phrase.count >= 3 && phrase.text.length >= 10)) {
      return {
        isValid: false,
        message: '같은 내용이 너무 많이 반복되었어요. 더 다양하고 다채로운 이야기를 전해보는 게 어떨까요?'
      };
    }

    return {
      isValid: true,
      message: ''
    };
  },

  findSignificantRepeats(text) {
    const phrases = [];
    const minPhraseLength = 10;  // 최소 10글자 이상의 문장만 검사
    const words = text.split(/[\s,.!?]+/);  // 문장 부호와 공백으로 분리

    // 각 위치에서 시작하는 가능한 모든 구문 검사
    for (let len = minPhraseLength; len <= text.length / 2; len++) {
      for (let start = 0; start <= text.length - len; start++) {
        const phrase = text.slice(start, start + len);

        // 이미 처리된 구문이거나 의미 있는 구문이 아닌 경우 건너뛰기
        if (phrases.some(p => p.text.includes(phrase) || phrase.includes(p.text)) ||
            !this.isSignificantPhrase(phrase)) {
          continue;
        }

        // 구문의 출현 횟수 계산
        let count = 0;
        let pos = -1;
        while ((pos = text.indexOf(phrase, pos + 1)) !== -1) {
          count++;
        }

        if (count >= 3) {  // 3번 이상 반복되는 경우만 저장
          phrases.push({
            text: phrase,
            count: count
          });
        }
      }
    }

    return phrases.sort((a, b) => (b.text.length * b.count) - (a.text.length * a.count));
  },

  isSignificantPhrase(phrase) {
    return (
        phrase.length >= 10 && // 최소 길이
        phrase.trim().split(/\s+/).length >= 2 && // 최소 2개 이상의 단어
        !/^\s*$/.test(phrase) && // 공백으로만 이루어지지 않음
        !/^[.,!?;\s]*$/.test(phrase) && // 문장 부호로만 이루어지지 않음
        phrase.replace(/\s/g, '').length > phrase.length * 0.5 // 50% 이상이 실제 문자
    );
  }
};

if (typeof window !== 'undefined') {
  window.CommonUtils = window.CommonUtils || {};
  window.CommonUtils.ContentValidator = ContentValidator;
}

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

  initializeCharacterCounters() {
    const letterTextarea = document.getElementById('manito-letter-text-input');
    const letterCount500Display = document.getElementById('count500');
    if (letterTextarea && letterCount500Display) {
      new CharacterCounter(letterTextarea, letterCount500Display, 500);
    }

    const musicCommentTextarea = document.getElementById('manito-music-comment-input');
    const count100Display = document.getElementById('count100');
    if (musicCommentTextarea && count100Display) {
      new CharacterCounter(musicCommentTextarea, count100Display, 100);
    }

    const postTextarea = document.getElementById('new-post-content');
    const postCount500Display = postTextarea?.closest('.new-post-text-container')?.querySelector('.letter-count');
    if (postTextarea && postCount500Display) {
      new CharacterCounter(postTextarea, postCount500Display, 500);
      postTextarea.setAttribute('maxlength', '500');
    }

    const replyTextarea = document.getElementById('new-reply-content');
    const replyCount500Display = replyTextarea?.closest('.new-post-text-container')?.querySelector('.letter-count');
    if (replyTextarea && replyCount500Display) {
      new CharacterCounter(replyTextarea, replyCount500Display, 500);
      replyTextarea.setAttribute('maxlength', '500');
    }
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
      img.src = img.getAttribute('data-unchecked-src').replace('@{', '').replace('}', '');
      element.style.opacity = '0.3';
    } else {
      img.src = img.getAttribute('data-checked-src').replace('@{', '').replace('}', '');
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

  initializePreviousButton() {
    const previousButton = document.querySelector('.previous-button');
    if (previousButton) {
      previousButton.addEventListener('click', () => {
        // 브라우저 히스토리가 있는 경우
        if (window.history.length > 1) {
          window.history.back();
        } else {
          // 히스토리가 없는 경우 (직접 URL로 접근한 경우) 기본 페이지로 이동
          window.location.href = '/';
        }
      });
    }
  },

  initializePageModals() {
    try {
      if (document.getElementById("newPostFormModalContainer")) {
        new PostFormModal();
      }
      if (document.getElementById("profileUpdateModalContainer")) {
        new ProfileUpdateModal();
      }
      // 이전 버튼 초기화 추가
      this.initializePreviousButton();
    } catch (error) {
      console.error('모달 초기화 중 오류 발생:', error);
    }
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
      newConfirmBtn.addEventListener('click', () => {
        warningPopup.style.display = 'none';
      });
      warningConfirmBtn.parentNode.replaceChild(newConfirmBtn,
          warningConfirmBtn);
    }
  }
};

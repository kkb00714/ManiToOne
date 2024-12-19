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
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && this.modal?.style.display === 'block') {
        this.close();
      }
    });

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

    // 1. 키보드 패턴 검사 (예: asdf, qwer 등의 연속 입력)
    const keyboardPatterns = [
      /[asdf]{4,}/i,  // asdf 패턴
      /[qwer]{4,}/i,  // qwer 패턴
      /[zxcv]{4,}/i,  // zxcv 패턴
      /[hjkl]{4,}/i,  // hjkl 패턴
      /[yuio]{4,}/i   // yuio 패턴
    ];

    if (keyboardPatterns.some(pattern => pattern.test(trimmedText))) {
      return {
        isValid: false,
        message: '키보드를 무작위로 입력한 것 같아요. 진심을 담아 작성해주세요.'
      };
    }

    // 2. 자음/모음 비율 검사 (한글)
    const koreanConsonants = trimmedText.match(/[ㄱ-ㅎ]/g) || [];
    const koreanVowels = trimmedText.match(/[ㅏ-ㅣ]/g) || [];
    const koreanChars = trimmedText.match(/[가-힣]/g) || [];

    if (koreanConsonants.length + koreanVowels.length > koreanChars.length * 0.5) {
      return {
        isValid: false,
        message: '완성되지 않은 한글이 너무 많아요. 정확한 단어로 작성해주세요.'
      };
    }

    // 3. 무작위 문자열 패턴 검사
    const randomnessCheck = this.checkRandomness(trimmedText);
    if (!randomnessCheck.isValid) {
      return randomnessCheck;
    }

    // 4. 연속된 같은 문자 패턴 검사 (30회 이상)
    const sameCharPattern = /(.)\1{29,}/;
    if (sameCharPattern.test(trimmedText)) {
      return {
        isValid: false,
        message: '같은 문자가 과도하게 반복되었어요. 정성을 담아 작성해주세요.'
      };
    }

    // 5. 자음만 연속으로 10회 이상 반복되는 패턴 검사
    const consonantsPattern = /([ㄱ-ㅎ]){10,}/;
    if (consonantsPattern.test(trimmedText)) {
      return {
        isValid: false,
        message: '자음만 연속적으로 입력되었어요. 의미 있는 내용을 작성해주세요.'
      };
    }

    // 6. 모음만 연속으로 10회 이상 반복되는 패턴 검사
    const vowelsPattern = /([ㅏ-ㅣ]){10,}/;
    if (vowelsPattern.test(trimmedText)) {
      return {
        isValid: false,
        message: '모음만 연속적으로 입력되었어요. 의미 있는 내용을 작성해주세요.'
      };
    }

    // 7. 특수문자나 이모티콘 연속 패턴 검사 (10회 이상)
    const specialCharsPattern = /([!@#$%^&*()_+=\-`~,./<>?;:'"\[\]{}\\|])\1{9,}/;
    const emojiPattern = /([\uD800-\uDBFF][\uDC00-\uDFFF])\1{9,}/;

    if (specialCharsPattern.test(trimmedText) || emojiPattern.test(trimmedText)) {
      return {
        isValid: false,
        message: '특수문자나 이모티콘이 과도하게 반복되었어요. 진정성 있는 메시지를 전달해보세요.'
      };
    }

    // 8. 공백 문자 과다 사용 검사 (40% 이상)
    const nonWhitespaceLength = trimmedText.replace(/\s/g, '').length;
    if (nonWhitespaceLength < trimmedText.length * 0.6) {
      return {
        isValid: false,
        message: '내용에 공백이 너무 많아요. 당신의 진심을 담아 작성해주세요.'
      };
    }

    // 9. 문장 반복 패턴 검사
    const repeatingPhrases = this.findSignificantRepeats(trimmedText);
    const totalLength = trimmedText.length;
    const totalRepeatedLength = repeatingPhrases.reduce((sum, phrase) =>
        sum + (phrase.text.length * (phrase.count - 1)), 0);

    if (totalRepeatedLength / totalLength > 0.4 && repeatingPhrases.some(phrase =>
        phrase.count >= 3 && phrase.text.length >= 5)) {
      return {
        isValid: false,
        message: '비슷한 내용이 너무 많이 반복되었어요. 다양한 이야기를 전달해보세요.'
      };
    }

    return {
      isValid: true,
      message: ''
    };
  },

  checkRandomness(text) {
    // 1. 연속된 자음 빈도 검사
    const consonantGroups = text.match(/[bcdfghjklmnpqrstvwxyz]{5,}/gi) || [];
    if (consonantGroups.length > 0) {
      return {
        isValid: false,
        message: '의미 없는 자음 조합이 감지되었어요. 올바른 단어를 사용해주세요.'
      };
    }

    // 2. 문자 다양성 검사 - 더 관대한 임계값 적용
    const chars = text.toLowerCase().replace(/\s/g, '').split('');
    const uniqueChars = new Set(chars);
    const diversity = uniqueChars.size / chars.length;

    // 문자 다양성 임계값을 0.8에서 0.9로 상향 조정
    // 길이가 더 긴 텍스트의 경우 다양성이 자연스럽게 높아질 수 있음을 고려
    const diversityThreshold = chars.length > 100 ? 0.9 : 0.85;

    if (diversity > diversityThreshold && chars.length > 20) {
      // 영어 문장 패턴 검사 추가
      const words = text.split(/\s+/);
      const validWordCount = words.filter(word => this.isValidEnglishWord(word)).length;
      const validWordRatio = validWordCount / words.length;

      // 60% 이상의 단어가 유효한 영어 단어 패턴을 가지면 유효한 것으로 판단
      if (validWordRatio < 0.6) {
        return {
          isValid: false,
          message: '무작위로 입력된 것 같아요. 의미 있는 내용을 작성해주세요.'
        };
      }
    }

    // 3. 알파벳 분포 검사 개선
    const alphabetCount = text.match(/[a-zA-Z]/g)?.length || 0;
    const totalCount = text.replace(/\s/g, '').length;

    if (alphabetCount / totalCount > 0.7) {
      const words = text.split(/\s+/);
      const validWords = words.filter(word => this.isValidEnglishWord(word));

      if (validWords.length < words.length * 0.4) {  // 임계값을 0.3에서 0.4로 조정
        return {
          isValid: false,
          message: '의미 없는 영문자 조합이 감지되었어요. 올바른 단어를 사용해주세요.'
        };
      }
    }

    return {
      isValid: true,
      message: ''
    };
  },

  isValidEnglishWord(word) {
    // 특수문자 제거
    word = word.replace(/[^a-zA-Z]/g, '').toLowerCase();

    if (word.length < 2) return true;  // 1글자는 통과

    // 개선된 영단어 패턴 검사
    // 1. 일반적인 영어 단어 패턴
    const commonPattern = /^[a-z]+$/;
    // 2. 최소한 하나의 모음을 포함
    const hasVowel = /[aeiou]/;
    // 3. 허용되지 않는 자음 조합 패턴
    const invalidConsonants = /[bcdfghjklmnpqrstvwxyz]{5,}/;
    // 4. 같은 문자가 3번 이상 연속되는 패턴
    const repeatingChars = /(.)\1{2,}/;

    return commonPattern.test(word) &&
        hasVowel.test(word) &&
        !invalidConsonants.test(word) &&
        !repeatingChars.test(word);
  },

  findSignificantRepeats(text) {
    // 기존 코드 유지
    const phrases = [];
    const minPhraseLength = 5;
    const maxPhraseLength = Math.floor(text.length / 2);

    for (let len = minPhraseLength; len <= maxPhraseLength; len++) {
      for (let start = 0; start <= text.length - len; start++) {
        const phrase = text.slice(start, start + len);

        if (phrases.some(p => p.text.includes(phrase) || phrase.includes(p.text)) ||
            !this.isSignificantPhrase(phrase)) {
          continue;
        }

        let count = 0;
        let pos = 0;
        while ((pos = text.indexOf(phrase, pos)) !== -1) {
          count++;
          pos += phrase.length;
        }

        if (count >= 3) {
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
        phrase.length >= 5 &&
        phrase.trim().length > 0 &&
        phrase.replace(/\s/g, '').length > phrase.length * 0.5 &&
        !/^[!@#$%^&*()_+=\-`~,./<>?;:'"\[\]{}\\|]+$/.test(phrase) &&
        !/^[\uD800-\uDBFF][\uDC00-\uDFFF]+$/.test(phrase) &&
        !/^[ㄱ-ㅎ]+$/.test(phrase) &&
        !/^[ㅏ-ㅣ]+$/.test(phrase)
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
        if (window.history.length > 1) {
          window.history.back();
        } else {
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

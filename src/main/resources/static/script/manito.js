// manito.js

class ManitoLetterModal extends BaseModal {
  constructor(modalId, backgroundId, openBtnId, closeBtnId) {
    super(modalId, backgroundId, openBtnId, closeBtnId);
    this.initializeSendConfirmation();
    this.postId = document.querySelector('meta[name="post-id"]')?.content;
  }

  isValidYoutubeUrl(url) {
    if (!url || url.trim() === '') {
      return true;
    }
    const youtubePattern = /^(https?:\/\/)?(www\.)?(youtube\.com\/watch\?v=|youtu\.be\/)[a-zA-Z0-9_-]{11}$/;
    return youtubePattern.test(url.trim());
  }

  validateForm() {
    const letterText = this.modal.querySelector('#manito-letter-text-input');
    const musicUrl = this.modal.querySelector('#music-link-input');
    const musicComment = this.modal.querySelector(
        '#manito-music-comment-input');

    if (!letterText.value.trim()) {
      this.showWarning('편지 내용을 작성해주세요.');
      letterText.focus();
      return false;
    }

    if (letterText.value.length > 500) {
      this.showWarning('편지는 500자를 초과할 수 없습니다.');
      letterText.focus();
      return false;
    }

    if (musicUrl.value.trim() && !this.isValidYoutubeUrl(musicUrl.value)) {
      this.showWarning('올바른 YouTube URL 형식이 아닙니다.');
      musicUrl.focus();
      return false;
    }

    if (musicUrl.value.length > 200) {
      this.showWarning('음악 URL은 200자를 초과할 수 없습니다.');
      musicUrl.focus();
      return false;
    }

    if (musicComment.value.length > 100) {
      this.showWarning('음악 추천 이유는 100자를 초과할 수 없습니다.');
      musicComment.focus();
      return false;
    }

    return true;
  }

  async updateLetterButton() {
    try {
      const sendButton = document.getElementById('openManitoLetterModalBtn');
      if (!sendButton) {
        return;
      }

      const existingButton = document.createElement('button');
      existingButton.className = 'reply-button';
      existingButton.style.cssText = 'display: flex; padding: 0.5rem 1.7rem; font-size: 1.3rem; margin: 0 auto 2rem;';
      existingButton.textContent = '이미 편지를 작성하셨습니다';
      existingButton.disabled = true;

      sendButton.parentNode.replaceChild(existingButton, sendButton);
    } catch (error) {
      console.error('Error updating button state:', error);
    }
  }

  async sendLetter() {
    const letterText = this.modal.querySelector(
        '#manito-letter-text-input').value;
    const musicUrl = this.modal.querySelector('#music-link-input').value;
    const musicComment = this.modal.querySelector(
        '#manito-music-comment-input').value;

    try {
      const response = await fetch(`/api/manito/letter/${this.postId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          letterContent: letterText.trim(),
          musicUrl: musicUrl.trim(),
          musicComment: musicComment.trim()
        })
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || '편지 전송에 실패했습니다.');
      }

      if (window.ManitoPage && window.ManitoPage.letterBox) {
        const letterBox = window.ManitoPage.letterBox;

        letterBox.activeTab = 'sent';

        const receivedTab = document.querySelector('.received-letter-box');
        const sentTab = document.querySelector('.sent-letter-box');
        const switchElement = document.querySelector(
            '.manito-letter-box-switch');

        if (receivedTab && sentTab && switchElement) {
          receivedTab.classList.remove('active');
          sentTab.classList.add('active');
          switchElement.classList.remove('received-active');
          switchElement.classList.add('sent-active');
        }

        letterBox.currentPage = 0;
        letterBox.hasMore = true;
        letterBox.container.innerHTML = '';
        await letterBox.loadMoreLetters();
      }

      await this.updateLetterButton();

      return true;
    } catch (error) {
      console.error('Error sending letter:', error);
      this.showWarning(error.message || '편지 전송 중 오류가 발생했습니다.');
      return false;
    }
  }

  initializeSendConfirmation() {
    const sendButton = this.modal.querySelector('.send-letter-button');
    if (!sendButton) {
      return;
    }

    const confirmationPopup = document.getElementById(
        'letterConfirmationPopup');
    const successPopup = document.getElementById('letterSuccessPopup');
    const confirmBtn = document.getElementById('letterConfirmSendBtn');
    const cancelBtn = document.getElementById('letterCancelSendBtn');
    const successConfirmBtn = document.getElementById(
        'letterSuccessConfirmBtn');

    if (confirmationPopup && successPopup && confirmBtn && cancelBtn
        && successConfirmBtn) {
      sendButton.addEventListener('click', async (e) => {
        e.preventDefault();
        if (this.validateForm()) {
          confirmationPopup.style.display = 'block';
        }
      });

      confirmBtn.onclick = async (e) => {
        e.preventDefault();
        confirmationPopup.style.display = 'none';

        const success = await this.sendLetter();
        if (success) {
          successPopup.style.display = 'block';
        }
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

// 마니또 페이지 관련 기능을 관리
const ManitoPage = {
  letterBox: {
    currentPage: 0,
    isLoading: false,
    hasMore: true,
    activeTab: 'received',
    container: null,
    scrollContainer: null,
    receivedTab: null,
    sentTab: null,

    async init() {
      this.container = document.querySelector('.manito-letter-box-content');
      this.scrollContainer = document.querySelector(
          '.manito-letter-box-container');
      this.receivedTab = document.querySelector('.received-letter-box');
      this.sentTab = document.querySelector('.sent-letter-box');

      if (!this.container || !this.scrollContainer) {
        return;
      }

      this.setupEventListeners();
      await this.loadInitialLetters();
    },

    async refreshLetterBox() {
      const currentTab = this.activeTab;

      this.currentPage = 0;
      this.hasMore = true;
      this.container.innerHTML = '';

      if (currentTab === 'received') {
        this.receivedTab.classList.add('active');
        this.sentTab.classList.remove('active');
        document.querySelector('.manito-letter-box-switch').classList.remove(
            'sent-active');
        document.querySelector('.manito-letter-box-switch').classList.add(
            'received-active');
      } else {
        this.sentTab.classList.add('active');
        this.receivedTab.classList.remove('active');
        document.querySelector('.manito-letter-box-switch').classList.remove(
            'received-active');
        document.querySelector('.manito-letter-box-switch').classList.add(
            'sent-active');
      }

      await this.loadMoreLetters();
    },

    setupEventListeners() {
      this.receivedTab.addEventListener('click', () => {
        this.switchTab('received');
        this.receivedTab.classList.add('active');
        this.sentTab.classList.remove('active');
        document.querySelector('.manito-letter-box-switch').classList.remove(
            'sent-active');
        document.querySelector('.manito-letter-box-switch').classList.add(
            'received-active');
      });

      this.sentTab.addEventListener('click', () => {
        this.switchTab('sent');
        this.sentTab.classList.add('active');
        this.receivedTab.classList.remove('active');
        document.querySelector('.manito-letter-box-switch').classList.remove(
            'received-active');
        document.querySelector('.manito-letter-box-switch').classList.add(
            'sent-active');
      });

      this.scrollContainer.addEventListener('scroll', () => {
        const {scrollTop, scrollHeight, clientHeight} = this.scrollContainer;
        if (scrollHeight - scrollTop <= clientHeight + 100) {
          this.loadMoreLetters();
        }
      });

      this.receivedTab.classList.add('active');
      document.querySelector('.manito-letter-box-switch').classList.add(
          'received-active');
    },

    async switchTab(tabName) {
      if (this.activeTab === tabName || this.isLoading) {
        return;
      }

      this.activeTab = tabName;
      this.currentPage = 0;
      this.hasMore = true;

      await this.loadInitialLetters();
    },

    async loadInitialLetters() {
      this.container.innerHTML = '';
      await this.loadMoreLetters();
    },

    async loadMoreLetters() {
      if (this.isLoading || !this.hasMore) {
        return;
      }

      this.isLoading = true;
      this.showLoader();

      try {
        const userNickname = document.querySelector(
            'meta[name="user-nickname"]')?.content;

        //개발 도중
        if (!userNickname) {
          this.container.innerHTML = `
                <div class="empty-letter-message">
                    <p>사용자 정보를 찾을 수 없습니다.</p>
                </div>
            `;
          return;
        }

        const endpoint = this.activeTab === 'received'
            ? `/api/receivemanito/${userNickname}`
            : `/api/sendmanito/${userNickname}`;

        const response = await fetch(
            `${endpoint}?page=${this.currentPage}&size=10`);
        if (!response.ok) {
          throw new Error('Failed to fetch letters');
        }

        const data = await response.json();

        if (data.content.length === 0 && this.currentPage === 0) {
          const message = this.activeTab === 'received'
              ? '아직 받은 편지가 없습니다.'
              : '아직 보낸 편지가 없습니다.';

          this.container.innerHTML = `
            <div class="empty-letter-message">
              <p>${message}</p>
            </div>
          `;
          this.hasMore = false;
          return;
        } else if (data.content.length === 0) {
          this.hasMore = false;
          return;
        }

        for (const letter of data.content) {
          const letterHTML = await this.createLetterHTML(letter);
          this.container.insertAdjacentHTML('beforeend', letterHTML);
        }

        this.currentPage++;
        this.hasMore = data.hasNext;

      } catch (error) {
        console.error('Error loading letters:', error);
        if (this.currentPage === 0) {
          this.container.innerHTML = `
            <div class="empty-letter-message">
              <p>편지를 불러오는 중 오류가 발생했습니다.</p>
            </div>
          `;
        }
      } finally {
        this.isLoading = false;
        this.hideLoader();
      }
    },

    showLoader() {
      if (!document.querySelector('.letter-loader')) {
        const loader = document.createElement('div');
        loader.className = 'letter-loader';
        loader.innerHTML = `
      <div class="loader-content">
        <span class="loader-text">로딩 중...</span>
      </div>
    `;
        this.container.appendChild(loader);
      }
    },

    hideLoader() {
      const loader = document.querySelector('.letter-loader');
      if (loader) {
        loader.remove();
      }
    },

    createLetterHTML(letter) {
      return ManitoLetterRenderer.generateLetterHTML(letter,
          this.activeTab === 'received');
    }
  },

  // 모달 관련 기능
  modals: {
    letterModal: null,
    replyModal: null,

    init() {
      const letterModalContainer = document.getElementById(
          "manitoLetterModalContainer");
      if (letterModalContainer) {
        this.letterModal = new ManitoLetterModal(
            "manitoLetterModalContainer",
            "manitoLetterModalBackground",
            "openManitoLetterModalBtn",
            "closeManitoLetterModalBtn"
        );
      }

      const replyModalContainer = document.getElementById(
          "manitoLetterReplyModalContainer");
      if (replyModalContainer) {
        this.replyModal = new ManitoLetterReplyModal(
            "manitoLetterReplyModalContainer",
            "manitoLetterReplyModalBackground",
            null,
            "closeManitoLetterReplyModalBtn"
        );
      }

      this.initializeReplySentModal();
    },

    initializeReplySentModal() {
      const modalBackground = document.getElementById(
          'manitoLetterReplySentModalBackground');
      const modalContainer = document.getElementById(
          'manitoLetterReplySentModalContainer');
      const closeButton = document.getElementById(
          'closeManitoLetterReplySentModalBtn');

      if (closeButton && modalContainer && modalBackground) {
        const closeModal = () => {
          modalBackground.style.display = 'none';
          modalContainer.style.display = 'none';
          document.body.style.overflow = '';
          document.body.style.paddingRight = '';
        };

        closeButton.onclick = closeModal;
        modalBackground.onclick = (e) => {
          if (e.target === modalBackground) {
            closeModal();
          }
        };
      }
    },

    async openReplyModal(letterId) {
      if (!this.replyModal) {
        return;
      }

      try {
        const response = await fetch(`/api/manito/letter/${letterId}`);
        const letter = await response.json();

        if (letter.answerLetter) {
          this.showWarningMessage('이미 답장을 보낸 편지입니다.');
          return;
        }

        this.replyModal.letterId = letterId;
        this.replyModal.resetForm();
        this.replyModal.open();

      } catch (error) {
        console.error('Error checking letter:', error);
        this.showWarningMessage('편지 정보를 확인하는데 실패했습니다.');
      }
    },

    async openSentReplyModal(letterId, isMyReply = true) {
      try {
        const response = await fetch(`/api/manito/letter/${letterId}`);

        if (!response.ok) {
          throw new Error('답장을 불러오는데 실패했습니다.');
        }

        const letterData = await response.json();

        const modalContainer = document.getElementById(
            'manitoLetterReplySentModalContainer');
        const modalBackground = document.getElementById(
            'manitoLetterReplySentModalBackground');
        const modalTitle = modalContainer.querySelector(
            '.send-letter-reply-title p');
        const replyTextElement = modalContainer.querySelector(
            '.manito-letter-reply-text');

        if (!modalContainer || !modalBackground || !replyTextElement
            || !modalTitle) {
          throw new Error('모달 요소를 찾을 수 없습니다.');
        }

        modalTitle.textContent = isMyReply ? '내가 보낸 답장' : '마니또의 답장';

        replyTextElement.innerHTML = letterData.answerLetter?.replace(/\n/g,
            '<br>') || '';

        const scrollbarWidth = window.innerWidth
            - document.documentElement.clientWidth;
        document.body.style.overflow = 'hidden';
        document.body.style.paddingRight = `${scrollbarWidth}px`;

        modalContainer.style.display = 'block';
        modalBackground.style.display = 'block';

      } catch (error) {
        console.error('Error displaying reply:', error);
        this.showWarningMessage(error.message);
      }
    },

    showWarningMessage(message) {
      const warningPopup = document.getElementById('warningPopup');
      const warningMessage = document.getElementById('warningMessage');

      if (warningPopup && warningMessage) {
        warningMessage.textContent = message;
        warningPopup.style.display = 'block';

        setTimeout(() => {
          warningPopup.style.display = 'none';
        }, 3000);
      }
    }
  },

  async toggleLetterVisibility(letterId, toggleButton) {
    try {
      const response = await fetch(`/api/manito/hide/letter/${letterId}`, {
        method: 'PUT'
      });

      if (!response.ok) {
        throw new Error('편지 공개 설정 변경에 실패했습니다.');
      }

      const img = toggleButton.querySelector('img');
      const isChecked = img.src.includes('icon-check.png');

      if (isChecked) {
        img.src = img.getAttribute('data-unchecked-src');
        toggleButton.style.opacity = '0.3';
      } else {
        img.src = img.getAttribute('data-checked-src');
        toggleButton.style.opacity = '1';
      }
    } catch (error) {
      console.error('Error toggling letter visibility:', error);
      this.modals.showWarningMessage(error.message);
    }
  },

  init() {
    this.letterBox.init();
    this.modals.init();
  }
};

class ManitoLetterRenderer {
  static generateLetterHTML(letter, isReceived = true) {
    const ownerClass = isReceived ? 'received' : 'sent';
    const buttonStyle = !isReceived ? 'style="margin-left: auto;"' : '';

    return `
      <div class="manito-reply-outer-container ${ownerClass}" data-letter-id="${letter.manitoLetterId}">
      <div class="manito-reply-container">
        <img class="manito-user-photo" src="/images/icons/icon-clover2.png" alt="anonymous user icon" />
        <div class="post-content">
          <div class="user-info">
            <span class="manito-user-name">익명의 마니또</span>
            <span class="passed-time">${letter.timeDiff}</span>
          </div>
          <p class="manito-content-text">${letter.letterContent}</p>
          <div class="manito-recommend-music">
            <div class="recommend-music">
              <img class="tiny-icons-linkless" src="/images/icons/icon-music.png" alt="music icon" />
              <span>추천하는 음악</span>
            </div>
            ${letter.musicUrl ? `
              <p class="music-link">
                <a href="${letter.musicUrl}" target="_blank">${letter.musicUrl}</a>
              </p>
            ` : ''}
          </div>
          ${letter.musicComment ? `
            <p class="manito-music-comment">${letter.musicComment}</p>
          ` : ''}
        </div>
        <div class="option-icons">
          <img class="tiny-icons" src="/images/icons/UI-more2.png" alt="more options" />
        </div>
      </div>
      <div class="manito-reaction-container">
        ${isReceived ? `
          <button class="manito-reply-toggle" onclick="ManitoPage.toggleLetterVisibility(${letter.manitoLetterId}, this)">
            <img class="tiny-icons" 
                src="/images/icons/icon-check-${letter.isPublic ? '' : 'empty'}.png" 
                alt="check icon"
                data-checked-src="/images/icons/icon-check.png"
                data-unchecked-src="/images/icons/icon-check-empty.png" />
            <span>이 마니또의 편지를 모두가 볼 수 있도록 공개합니다.</span>
          </button>
          ${!letter.answerLetter ? `
            <button class="reply-button" ${buttonStyle} onclick="ManitoPage.modals.openReplyModal(${letter.manitoLetterId})">
              답장하기
            </button>
          ` : `
            <button class="reply-button" ${buttonStyle} onclick="ManitoPage.modals.openSentReplyModal(${letter.manitoLetterId}, true)">
              내가 보낸 답장 보기
            </button>
          `}
        ` : `
          ${letter.answerLetter ? `
            <button class="reply-button" ${buttonStyle} onclick="ManitoPage.modals.openSentReplyModal(${letter.manitoLetterId}, false)">
              답장 확인하기
            </button>
          ` : ''}
        `}
      </div>
      </div>
    `;
  }
}

class ManitoLetterReplyModal extends BaseModal {
  constructor(modalId, backgroundId, openBtnId, closeBtnId) {
    super(modalId, backgroundId, openBtnId, closeBtnId);
    this.letterId = null;
    this.initializeSendConfirmation();
  }

  validateForm() {
    const replyText = this.modal.querySelector('#manito-letter-reply-input');

    if (!replyText) {
      console.error('Reply text input element not found');
      this.showWarning('답장 입력 필드를 찾을 수 없습니다.');
      return false;
    }

    if (!replyText.value.trim()) {
      this.showWarning('답장 내용을 작성해주세요.');
      replyText.focus();
      return false;
    }

    if (replyText.value.length > 500) {
      this.showWarning('답장은 500자를 초과할 수 없습니다.');
      replyText.focus();
      return false;
    }

    return true;
  }

  initializeSendConfirmation() {
    const sendButton = this.modal.querySelector('.send-letter-reply-button');
    if (!sendButton) {
      return;
    }

    const confirmationPopup = document.getElementById('sendConfirmationPopup');
    const successPopup = document.getElementById('sendSuccessPopup');
    const confirmBtn = document.getElementById('confirmSendBtn');
    const cancelBtn = document.getElementById('cancelSendBtn');
    const successConfirmBtn = document.getElementById('successConfirmBtn');

    if (confirmationPopup && successPopup && confirmBtn && cancelBtn
        && successConfirmBtn) {
      sendButton.onclick = (e) => {
        e.preventDefault();
        if (this.validateForm()) {
          confirmationPopup.style.display = 'block';
        }
      };

      confirmBtn.onclick = async (e) => {
        e.preventDefault();
        confirmationPopup.style.display = 'none';
        const success = await this.sendReply();
        if (success) {
          successPopup.style.display = 'block';
        }
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

  async sendReply() {
    if (!this.letterId) {
      this.showWarning('답장을 보낼 편지를 찾을 수 없습니다.');
      return false;
    }

    const replyText = this.modal.querySelector('#manito-letter-reply-input');
    if (!replyText) {
      return false;
    }

    try {
      const response = await fetch(`/api/manito/answer/${this.letterId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          answerComment: replyText.value.trim()
        })
      });

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(data.message || '이미 답장을 보냈거나 답장 전송에 실패했습니다.');
      }

      if (window.ManitoPage?.letterBox) {
        await window.ManitoPage.letterBox.refreshLetterBox();
      }

      return true;
    } catch (error) {
      console.error('Error sending reply:', error);
      this.showWarning(error.message);
      return false;
    }
  }
}

window.ManitoPage = ManitoPage;
document.addEventListener('DOMContentLoaded', () => {
  ManitoPage.init();
});
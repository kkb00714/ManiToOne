class ManitoLetterModal extends BaseModal {
  constructor(modalId, backgroundId, openBtnId, closeBtnId) {
    super(modalId, backgroundId, openBtnId, closeBtnId);
    this.initializeSendConfirmation();
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
      if (musicUrl && musicUrl.value.trim() && !this.isValidYoutubeUrl(
          musicUrl.value.trim())) {
        this.showWarning('Youtube url을 입력해주세요.');
        musicUrl.focus();
        return false;
      }
    }

    return true;
  }

  initializeSendConfirmation() {
    const sendButton = this.modal.querySelector(
        '.send-letter-button, .send-letter-reply-button');
    if (!sendButton) {
      return;
    }

    // 편지 모달인지 답장 모달인지 구분
    const isLetterModal = this.modal.id === 'manitoLetterModalContainer';
    const confirmationPopup = document.getElementById(
        isLetterModal ? 'letterConfirmationPopup' : 'sendConfirmationPopup');
    const successPopup = document.getElementById(
        isLetterModal ? 'letterSuccessPopup' : 'sendSuccessPopup');
    const confirmBtn = document.getElementById(
        isLetterModal ? 'letterConfirmSendBtn' : 'confirmSendBtn');
    const cancelBtn = document.getElementById(
        isLetterModal ? 'letterCancelSendBtn' : 'cancelSendBtn');
    const successConfirmBtn = document.getElementById(
        isLetterModal ? 'letterSuccessConfirmBtn' : 'successConfirmBtn');

    if (confirmationPopup && successPopup && confirmBtn && cancelBtn
        && successConfirmBtn) {
      sendButton.replaceWith(sendButton.cloneNode(true));
      const newSendButton = this.modal.querySelector(
          '.send-letter-button, .send-letter-reply-button');

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
        // TODO: API 호출 추가
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

// 마니또 페이지 관련 기능을 관리하는 객체
const ManitoPage = {
  // 편지함 관련 상태와 메서드
  letterBox: {
    currentPage: 0,
    isLoading: false,
    hasMore: true,
    activeTab: 'received',

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
      this.setupStyles();
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

      // 초기 상태 설정
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
        loader.innerHTML = '로딩 중...';
        this.container.appendChild(loader);
      }
    },

    hideLoader() {
      const loader = document.querySelector('.letter-loader');
      if (loader) {
        loader.remove();
      }
    },

    async createLetterHTML(letter) {
      try {
        const response = await fetch(
            `/view/fragments/manito-letter?letterId=${letter.manitoLetterId}`);
        if (!response.ok) {
          console.error(`Failed to fetch letter fragment: ${response.status}`);
          return '';
        }
        return await response.text();
      } catch (error) {
        console.error('Network error while fetching letter fragment:', error);
        return '';
      }
    },

    setupStyles() {
      if (!document.getElementById('manito-letter-box-styles')) {
        const style = document.createElement('style');
        style.id = 'manito-letter-box-styles';
        style.textContent = `
          .manito-letter-box-switch {
            position: relative;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 3px solid rgba(171, 171, 171, 0.25);
          }
          
          .manito-letter-box-switch::after {
            content: '';
            position: absolute;
            bottom: -3px;
            height: 3px;
            width: 50%;
            background-color: #3f624c;
            transition: transform 0.3s ease;
          }

          .manito-letter-box-switch.received-active::after {
            transform: translateX(0);
            left: 0;
          }
          
          .manito-letter-box-switch.sent-active::after {
            transform: translateX(100%);
            left: 0;
          }
          
          .received-letter-box,
          .sent-letter-box {
            flex: 1;
            text-align: center;
            padding: 1rem 0;
            cursor: pointer;
            transition: color 0.3s ease;
          }
          
          .received-letter-box.active,
          .sent-letter-box.active {
            color: #3f624c;
            font-weight: bold;
          }
          
          .empty-letter-message {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 200px;
            text-align: center;
            color: #8f8f8f;
            background-color: #f3f3f3;
            border-radius: 0 0 20px 20px;
            font-size: 0.95rem;
          }

          .empty-letter-message p {
            margin: 0;
            padding: 2rem;
          }
          
          .letter-loader {
            text-align: center;
            padding: 1rem;
            color: #3f624c;
          }
        `;
        document.head.appendChild(style);
      }
    }
  },

  // 모달 관련 기능
  modals: {
    init() {
      if (document.getElementById("manitoLetterModalContainer")) {
        new ManitoLetterModal(
            "manitoLetterModalContainer",
            "manitoLetterModalBackground",
            "openManitoLetterModalBtn",
            "closeManitoLetterModalBtn"
        );
      }
      if (document.getElementById("manitoLetterReplyModalContainer")) {
        new ManitoLetterModal(
            "manitoLetterReplyModalContainer",
            "manitoLetterReplyModalBackground",
            "openManitoLetterReplyModalBtn",
            "closeManitoLetterReplyModalBtn"
        );
      }
    }
  },

  // 마니또 관련 토글 기능
  toggleManito(element, type) {
    const img = element.querySelector('img');
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

  // 전체 페이지 초기화
  init() {
    this.letterBox.init();
    this.modals.init();
    window.toggleManito = this.toggleManito;
  }
};

window.ManitoPage = ManitoPage;
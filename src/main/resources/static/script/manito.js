// manito.js

class ManitoLetterModal extends BaseModal {
  constructor(modalId, backgroundId, openBtnId, closeBtnId) {
    super(modalId, backgroundId, openBtnId, closeBtnId);
    this.initializeSendConfirmation();
    this.userNameElement = this.modal.querySelector('.user-name');
    this.recipientName = this.userNameElement ? this.userNameElement.textContent
        : '';
    this.manitoMatchesId = document.querySelector(
        'meta[name="manito-matches-id"]')?.content;
  }

  isValidYoutubeUrl(url) {
    if (!url || url.trim() === '') {
      return true;
    }
    const youtubePattern = /^(?:https?:\/\/)?(?:www\.|m\.)?(?:youtube\.com\/(?:watch\?(?:.*&)?v=|v\/|embed\/)|youtu\.be\/)([\w-]{11})(?:[?&].*)?$/;
    return youtubePattern.test(url.trim());
  }

  validateForm() {
    const letterText = this.modal.querySelector('#manito-letter-text-input');
    const musicUrl = this.modal.querySelector('#music-link-input');
    const musicComment = this.modal.querySelector(
        '#manito-music-comment-input');

    const contentValidation = CommonUtils.ContentValidator.validateContentQuality(
        letterText.value);
    if (!contentValidation.isValid) {
      this.showWarning(contentValidation.message);
      letterText.focus();
      return false;
    }

    if (!letterText.value.trim()) {
      this.showWarning('편지 내용을 작성해주세요.');
      letterText.focus();
      return false;
    }

    if (letterText.value.trim().length < 200) {
      this.showWarning('편지는 200자 이상 작성해주세요.');
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

      const actionButtons = sendButton.closest('.action-buttons');
      if (actionButtons) {
        actionButtons.innerHTML = `
                <button class="reply-button"
                    style="display: flex; padding: 0.5rem 1.7rem; font-size: 1.3rem; margin: 0 auto;"
                    disabled>
                    이미 편지를 작성하셨습니다
                </button>
                <div class="remaining-time" id="remainingTime"></div>
            `;
      }
    } catch (error) {
      console.error('Error updating button state:', error);
    }
  }

  async sendLetter() {
    if (!this.manitoMatchesId) {
      this.showWarning('매칭 정보를 찾을 수 없습니다.');
      return false;
    }

    const letterText = this.modal.querySelector(
        '#manito-letter-text-input').value;
    const musicUrl = this.modal.querySelector('#music-link-input').value;
    const musicComment = this.modal.querySelector(
        '#manito-music-comment-input').value;

    try {
      const response = await fetch(`/api/manito/letter/${this.manitoMatchesId}`,
          {
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

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || '편지 전송에 실패했습니다.');
      }

      if (window.ManitoPage && window.ManitoPage.letterBox) {
        const letterBox = window.ManitoPage.letterBox;
        letterBox.activeTab = 'sent';
        await letterBox.refreshLetterBox();
      }

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

      if (CommonUtils) {
        await CommonUtils.loadRecentLetters(elements);
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

      const urlParams = new URLSearchParams(window.location.search);
      const initialTab = urlParams.get('tab');
      const initialLetterId = urlParams.get('letterId');

      this.setupEventListeners();

      if (initialTab === 'sent') {
        this.activeTab = 'sent';
        this.sentTab.classList.add('active');
        this.receivedTab.classList.remove('active');
        document.querySelector('.manito-letter-box-switch').classList.remove(
            'received-active');
        document.querySelector('.manito-letter-box-switch').classList.add(
            'sent-active');
      } else {
        this.activeTab = 'received';
        this.receivedTab.classList.add('active');
        this.sentTab.classList.remove('active');
        document.querySelector('.manito-letter-box-switch').classList.add(
            'received-active');
        document.querySelector('.manito-letter-box-switch').classList.remove(
            'sent-active');
      }

      await this.loadInitialLetters();

      if (initialLetterId) {
        setTimeout(() => {
          const letterElement = document.querySelector(
              `[data-letter-id="${initialLetterId}"]`);
          if (letterElement) {
            letterElement.scrollIntoView({behavior: 'smooth', block: 'center'});
            letterElement.style.backgroundColor = '#F1F1E3';
            setTimeout(() => {
              letterElement.style.backgroundColor = '';
            }, 1000);
          }
        }, 500);
      }
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
        // 세션 체크로 변경
        const sessionResponse = await fetch('/api/session-info');
        if (!sessionResponse.ok) {
          throw new Error('Failed to get user session');
        }
        const sessionData = await sessionResponse.json();
        const userNickname = sessionData.nickname;

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
          const letterHTML = this.createLetterHTML(letter);
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

  modals: {
    letterModal: null,
    replyModal: null,
    reportModal: null,

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

      const reportModalContainer = document.getElementById(
          "reportModalContainer");
      if (reportModalContainer) {
        this.reportModal = new ReportModal();
      }

      this.initializeReplySentModal();
      this.initializeReportSystem();
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

    async handleReport(letterId, reportType) {
      try {
        const statusResponse = await fetch(
            `/api/manito/report/status/${letterId}?type=${reportType}`);
        if (!statusResponse.ok) {
          throw new Error('신고 상태를 확인하는데 실패했습니다.');
        }

        const statusData = await statusResponse.json();

        if (statusData.reported) {
          const message = reportType === 'MANITO_LETTER'
              ? '이미 신고된 편지입니다.'
              : '이미 신고된 답장입니다.';
          this.showWarningMessage(message);
          return;
        }

        if (reportType === 'MANITO_ANSWER') {
          const letterResponse = await fetch(`/api/manito/letter/${letterId}`);
          if (!letterResponse.ok) {
            throw new Error('편지 정보를 불러오는데 실패했습니다.');
          }
          const letter = await letterResponse.json();

          if (!letter.answerLetter) {
            this.showWarningMessage('답장이 존재하지 않습니다.');
            return;
          }
        }

        if (window.ManitoPage?.modals.reportModal) {
          await window.ManitoPage.modals.reportModal.openWithTarget(letterId,
              reportType);
        }

        document.querySelectorAll('.manito-report-menu').forEach(menu => {
          menu.style.display = 'none';
        });
      } catch (error) {
        console.error("Error handling report:", error);
        this.showWarningMessage(error.message);
      }
    },

    initializeReportSystem() {
      if (!this.reportModal) {
        return;
      }

      document.addEventListener('click', (e) => {
        const moreOptionsBtn = e.target.closest(
            '.tiny-icons[src*="UI-more2.png"]');
        if (moreOptionsBtn) {
          e.stopPropagation();
          document.querySelectorAll('.manito-report-menu').forEach(menu => {
            menu.style.display = 'none';
          });
          const reportMenu = moreOptionsBtn.parentElement.querySelector(
              '.manito-report-menu');
          if (reportMenu) {
            reportMenu.style.display = 'block';
          }
        }
      });

      document.addEventListener('click', async (e) => {
        const reportBtn = e.target.closest('.open-report-modal-btn');
        if (!reportBtn) {
          return;
        }

        const letterContainer = reportBtn.closest(
            '.manito-reply-outer-container');
        if (!letterContainer) {
          return;
        }

        const letterId = letterContainer.dataset.letterId;
        const reportType = letterContainer.dataset.reportType;

        await this.handleReport(letterId, reportType);
      });

      document.addEventListener('click', (e) => {
        if (!e.target.closest('.manito-report-menu') &&
            !e.target.closest('.tiny-icons[src*="UI-more2.png"]')) {
          document.querySelectorAll('.manito-report-menu').forEach(menu => {
            menu.style.display = 'none';
          });
        }
      });
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

        if (!isMyReply && letterData.answerReport) {
          CommonUtils.showWarningMessage('신고된 답장입니다.');
          return;
        }

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

        if (!isMyReply && letterData.answerReport) {
          replyTextElement.innerHTML = '<em style="color: #8f8f8f; font-style: italic;">신고된 답장입니다.</em>';
        } else {
          replyTextElement.innerHTML = letterData.answerLetter?.replace(/\n/g,
              '<br>') || '';
        }

        const moreOptionsBtn = modalContainer.querySelector(
            '.tiny-icons[src*="UI-more2.png"]')?.parentElement;
        const reportMenu = modalContainer.querySelector('.manito-report-menu');

        if (isMyReply) {
          if (moreOptionsBtn) {
            moreOptionsBtn.style.display = 'none';
          }
          if (reportMenu) {
            reportMenu.style.display = 'none';
          }
        }

        const scrollbarWidth = window.innerWidth
            - document.documentElement.clientWidth;
        document.body.style.overflow = 'hidden';
        document.body.style.paddingRight = `${scrollbarWidth}px`;
        modalContainer.style.display = 'block';
        modalBackground.style.display = 'block';

        if (!isMyReply) {
          const moreOptionsBtn = modalContainer.querySelector(
              '.tiny-icons[src*="UI-more2.png"]');
          const reportMenu = modalContainer.querySelector(
              '.manito-report-menu');
          const reportBtn = modalContainer.querySelector(
              '.open-report-modal-btn');

          if (moreOptionsBtn) {
            moreOptionsBtn.onclick = (e) => {
              e.stopPropagation();
              reportMenu.style.display = 'block';
            };
          }

          if (reportBtn) {
            reportBtn.replaceWith(reportBtn.cloneNode(true));
            const newReportBtn = modalContainer.querySelector(
                '.open-report-modal-btn');

            newReportBtn.addEventListener('click', async (e) => {
              e.preventDefault();
              const statusResponse = await fetch(
                  `/api/manito/report/status/${letterId}?type=MANITO_ANSWER`);
              if (!statusResponse.ok) {
                throw new Error('신고 상태를 확인하는데 실패했습니다.');
              }
              const statusData = await statusResponse.json();

              if (statusData.reported) {
                this.showWarningMessage('이미 신고된 답장입니다.');
                reportMenu.style.display = 'none';
                return;
              }

              await this.handleReport(letterId, 'MANITO_ANSWER');
              reportMenu.style.display = 'none';
            });
          }
        }

      } catch (error) {
        console.error('Error displaying reply:', error);
        CommonUtils.showWarningMessage(error.message);
      }
    },

    showWarningMessage(message) {
      const warningPopup = document.getElementById('warningPopup');
      const warningMessage = document.getElementById('warningMessage');
      const warningConfirmBtn = document.getElementById('warningConfirmBtn');

      if (warningPopup && warningMessage && warningConfirmBtn) {
        warningMessage.textContent = message;
        warningPopup.style.display = 'block';

        const newConfirmButton = warningConfirmBtn.cloneNode(true);
        warningConfirmBtn.parentNode.replaceChild(newConfirmButton,
            warningConfirmBtn);

        newConfirmButton.addEventListener('click', () => {
          warningPopup.style.display = 'none';
        });
      }
    }
  },

  async toggleLetterVisibility(letterId, toggleButton) {
    try {
      const checkResponse = await fetch(`/api/manito/letter/${letterId}`);
      if (!checkResponse.ok) {
        throw new Error('편지 정보를 불러오는데 실패했습니다.');
      }
      const letterData = await checkResponse.json();

      const response = await fetch(`/api/manito/hide/letter/${letterId}`, {
        method: 'PUT'
      });

      if (!response.ok) {
        throw new Error('편지 공개 설정 변경에 실패했습니다.');
      }

      const img = toggleButton.querySelector('img');
      const isCurrentlyPublic = letterData.public;

      if (isCurrentlyPublic) {
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


  initializePostNavigation() {
    const postContent = document.querySelector('.todays-manito-post .content-text');
    if (postContent) {
      postContent.addEventListener('click', function() {
        const postId = this.dataset.postId;
        if (postId) {
          location.href = `/post/${postId}`;
        }
      });
    }
  },

  init() {
    this.letterBox.init();
    this.modals.init();
    this.initializePostNavigation();
  }
};

class ManitoLetterRenderer {
  static generateLetterHTML(letter, isReceived = true) {

    if (isReceived && letter.report) {
      return `
        <div class="manito-reply-outer-container ${isReceived ? 'received'
          : 'sent'}" data-letter-id="${letter.manitoLetterId}">
            <div class="reported-content">
                <p>신고된 편지입니다</p>
            </div>
        </div>`;
    }

    const ownerClass = isReceived ? 'received' : 'sent';

    const isChecked = Boolean(letter.public);
    const checkIconSrc = isChecked ? '/images/icons/icon-check.png'
        : '/images/icons/icon-check-empty.png';
    const buttonOpacity = isChecked ? '1' : '0.3';

    const statusStyle = isChecked ? '' : 'opacity: 0.3;';

    let musicCommentText = letter.musicComment;
    if (!letter.musicComment) {
      if (!letter.musicUrl) {
        musicCommentText = '추천 음악이 없습니다';
      } else {
        musicCommentText = '코멘트가 없습니다';
      }
    }

    return `
     <div class="manito-reply-outer-container ${ownerClass}" data-letter-id="${letter.manitoLetterId}" data-report-type="${isReceived
        ? 'MANITO_LETTER' : 'MANITO_ANSWER'}">
      <div class="manito-reply-container">
        <img class="manito-user-photo" src="/images/icons/icon-clover2.png" alt="anonymous user icon" />
        <div class="post-content">
          <div class="user-info">
            <span class="manito-user-name">익명의 마니또</span>
            <span class="passed-time">${letter.timeDiff}</span>
          </div>
          <p class="manito-content-text">${letter.letterContent?.replace(/\n/g,
        '<br>') || ''}</p>
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
          <p class="manito-music-comment" ${!letter.musicComment
        ? 'style="color: #8f8f8f; opacity: 0.7; font-style: italic;"' : ''}>
            ${musicCommentText}
        </p>
          ${letter.formattedCreatedAt ? `
          <p class="post-time">${letter.formattedCreatedAt}</p>
          ` : ''}
        </div>
        <div class="option-icons" style="position: relative;">
          <img class="tiny-icons" src="/images/icons/UI-more2.png" alt="more options" />
          <div class="manito-report-menu">
            <button class="open-report-modal-btn">신고하기</button>
          </div>
        </div>
      </div>
      <div class="manito-reaction-container">
        ${isReceived ? `
          <button class="manito-reply-toggle" onclick="ManitoPage.toggleLetterVisibility(${letter.manitoLetterId}, this)" style="opacity: ${buttonOpacity}">
            <img class="tiny-icons" 
                src="${checkIconSrc}" 
                alt="check icon"
                data-checked-src="/images/icons/icon-check.png"
                data-unchecked-src="/images/icons/icon-check-empty.png" />
            <span>이 마니또의 편지를 모두가 볼 수 있도록 공개합니다.</span>
          </button>
          ${!letter.answerLetter ? `
            <button class="reply-button" onclick="ManitoPage.modals.openReplyModal(${letter.manitoLetterId})">
              답장하기
            </button>
          ` : `
            <button class="reply-button" onclick="ManitoPage.modals.openSentReplyModal(${letter.manitoLetterId}, true)">
              내가 보낸 답장 보기
            </button>
          `}
        ` : `
          <div class="manito-visibility-status" style="display: flex; align-items: center; gap: 8px; ${statusStyle}">
            <img class="tiny-icons" src="${checkIconSrc}" alt="check icon" style="pointer-events: none;" />
            <span style="color: #3f624c; font-weight: bold;">${isChecked
        ? '수신자가 이 편지를 모두에게 공개했습니다.' : '이 편지는 공개되어 있지 않습니다.'}</span>
          </div>
          ${letter.answerLetter ? `
            <button class="reply-button" onclick="ManitoPage.modals.openSentReplyModal(${letter.manitoLetterId}, false)">
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
      return false;
    }

    const contentValidation = CommonUtils.ContentValidator.validateContentQuality(
        replyText.value);
    if (!contentValidation.isValid) {
      this.showWarning(contentValidation.message);
      replyText.focus();
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

    if (replyText.value.trim().length < 200) {
      this.showWarning('답장은 200자 이상 작성해주세요.');
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

class ReportModal extends BaseModal {
  constructor() {
    super(
        "reportModalContainer",
        "reportModal",
        null,
        "closeReportModalBtn"
    );
    this.targetId = null;
    this.reportType = null;
    this.reportObjectType = null;
    this.initializeEventListeners();
  }

  initializeEventListeners() {
    super.initializeEventListeners();

    this.background.addEventListener('click', (e) => {
      if (e.target === this.background) {
        this.close();
      }
    });

    const reportSendBtn = document.getElementById('reportSendBtn');
    if (reportSendBtn) {
      reportSendBtn.onclick = (e) => {
        e.preventDefault();
        this.submitReport();
      };
    }

    const reportTypeSelect = document.getElementById('report-type-select');
    if (reportTypeSelect) {
      reportTypeSelect.onchange = (e) => {
        this.reportType = e.target.value;
      };
    }
  }

  async submitReport() {
    if (!this.targetId || !this.reportType) {
      this.showWarning('신고 사유를 선택해주세요.');
      return;
    }

    try {
      const endpoint = this.reportObjectType === 'MANITO_ANSWER'
          ? `/api/manito/report/answer/${this.targetId}`
          : `/api/manito/report/${this.targetId}`;

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          reportType: this.reportType
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '신고 처리 중 오류가 발생했습니다.');
      }

      this.showWarning('신고가 접수되었습니다.');
      this.resetForm();
      this.close();

      const replySentModal = document.getElementById(
          'manitoLetterReplySentModalContainer');
      const replySentModalBackground = document.getElementById(
          'manitoLetterReplySentModalBackground');
      if (replySentModal && replySentModalBackground) {
        replySentModal.style.display = 'none';
        replySentModalBackground.style.display = 'none';
        document.body.style.overflow = '';
        document.body.style.paddingRight = '';
      }
    } catch (error) {
      console.error('Error submitting report:', error);
      this.showWarning(error.message);
    }
  }

  resetForm() {
    const reportTypeSelect = document.getElementById('report-type-select');
    if (reportTypeSelect) {
      reportTypeSelect.value = '';
    }
    this.targetId = null;
    this.reportType = null;
    this.reportObjectType = null;
  }

  async openWithTarget(targetId, reportObjectType = 'MANITO_LETTER') {
    try {
      const response = await fetch(`/api/manito/letter/${targetId}`);
      if (!response.ok) {
        throw new Error('편지 정보를 불러오는데 실패했습니다.');
      }

      const letter = await response.json();

      if (reportObjectType === 'MANITO_LETTER' && letter.report) {
        this.showWarning('이미 신고된 편지입니다.');
        return;
      }

      if (reportObjectType === 'MANITO_ANSWER' && letter.answerReport) {
        this.showWarning('이미 신고된 답장입니다.');
        return;
      }

      this.targetId = targetId;
      this.reportObjectType = reportObjectType;
      this.open();

    } catch (error) {
      console.error('Error checking report status:', error);
      this.showWarning(error.message);
    }
  }
}

async function requestMatch() {
  const originalContent = document.querySelector('.pre-match-container').innerHTML;
  try {
    // 매칭 진행 중 상태 표시
    document.querySelector('.pre-match-container').innerHTML = `
            <div class="pre-match-content">
                <h2 class="pre-match-title">매칭이 진행 중입니다...</h2>
            </div>
        `;

    // 매칭 요청
    const response = await fetch('/api/manito/match/request', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || '매칭 요청 중 오류가 발생했습니다.');
    }

    // 매칭이 시작되었음을 확인하기 위해 약간의 지연 후 상태 체크 시작
    await new Promise(resolve => setTimeout(resolve, 1000));

    // 매칭 상태 주기적 확인
    const checkMatchStatus = async () => {
      try {
        const statusResponse = await fetch('/api/manito/match/status');
        const statusData = await statusResponse.json();

        if (!statusData) {
          setTimeout(checkMatchStatus, 2000);
          return;
        }

        // 매칭 진행 중인 경우
        if (statusData.status === 'IN_PROGRESS') {
          setTimeout(checkMatchStatus, 2000);
          return;
        }

        // 매칭 실패한 경우
        if (statusData.status === 'FAILED') {
          if (statusData.message?.includes('매칭 가능한 게시글이 없습니다')) {
            CommonUtils.showWarningMessage('현재 마니또를 기다리고 있는 게시물이 없어요.');
            document.querySelector('.pre-match-container').innerHTML = originalContent;
          } else {
            CommonUtils.showWarningMessage(statusData.message);
            document.querySelector('.pre-match-container').innerHTML = originalContent;
          }
          return;
        }

        // 매칭이 완료된 경우에만 페이지 리로드
        if (statusData.status === 'COMPLETED') {
          window.location.reload();
          return;
        }

        // 그 외의 경우 계속 체크
        setTimeout(checkMatchStatus, 2000);

      } catch (error) {
        console.error('매칭 상태 확인 중 오류:', error);
        CommonUtils.showWarningMessage('매칭 상태 확인 중 오류가 발생했습니다.');
        document.querySelector('.pre-match-container').innerHTML = originalContent;
      }
    };

    checkMatchStatus();

  } catch (error) {
    console.error('매칭 요청 에러:', error);
    if (error.message?.includes('매칭 가능한 게시글이 없습니다')) {
      CommonUtils.showWarningMessage('현재 마니또를 기다리고 있는 게시물이 없어요.');
    } else {
      CommonUtils.showWarningMessage(error.message || '서버와의 통신 중 오류가 발생했습니다.');
    }
    document.querySelector('.pre-match-container').innerHTML = originalContent;
  }
}

function passMatch() {
  const confirmationPopup = document.getElementById('passPostConfirmPopup');
  if (!confirmationPopup) {
    console.error('Confirmation popup element not found');
    return;
  }

  const confirmMessage = confirmationPopup.querySelector('.warning-text');
  const confirmBtn = confirmationPopup.querySelector('#passPostConfirmBtn');
  const cancelBtn = confirmationPopup.querySelector('#passPostCancelBtn');

  if (!confirmMessage || !confirmBtn || !cancelBtn) {
    console.error('Required confirmation elements not found');
    return;
  }

  confirmMessage.innerHTML = `
<span style="font-size:1.2rem; margin-bottom: 0.5rem;">정말 이 게시물에 편지를 쓰지 않으시겠어요?</span>
 <span style="color: #303030;">편지 작성을 넘기더라도 24시간이 경과해야<br>새로운 게시물을 배정받을 수 있어요.</span>`;
  confirmBtn.textContent = "넘기기";
  cancelBtn.textContent = "취소";

  const newConfirmBtn = confirmBtn.cloneNode(true);
  newConfirmBtn.addEventListener('click', async () => {
    confirmationPopup.style.display = 'none';

    try {
      const currentMatchId = document.querySelector(
          'meta[name="manito-match-id"]')?.content;
      if (!currentMatchId) {
        CommonUtils.showWarningMessage('매칭 정보를 찾을 수 없습니다.');
        return;
      }

      const response = await fetch(`/api/manito/pass/${currentMatchId}`, {
        method: 'PUT'
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || '게시물을 넘기는데 실패했습니다.');
      }

      window.location.reload();
    } catch (error) {
      CommonUtils.showWarningMessage(error.message);
    }
  });
  confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

  const newCancelBtn = cancelBtn.cloneNode(true);
  newCancelBtn.addEventListener('click', () => {
    confirmationPopup.style.display = 'none';
  });
  cancelBtn.parentNode.replaceChild(newCancelBtn, cancelBtn);

  confirmationPopup.style.display = 'block';
}

// 남은 시간 표시
function updateRemainingTime() {
  const matchedTime = document.querySelector(
      'meta[name="matched-time"]')?.content;
  if (!matchedTime) {
    return;
  }

  const matchDate = new Date(matchedTime);
  const now = new Date();
  const timeLimit = new Date(matchDate.getTime() + 24 * 60 * 60 * 1000);
  const remainingTime = timeLimit - now;

  if (remainingTime > 0) {
    const hours = Math.floor(remainingTime / (60 * 60 * 1000));
    const minutes = Math.floor(
        (remainingTime % (60 * 60 * 1000)) / (60 * 1000));
    const seconds = Math.floor((remainingTime % (60 * 1000)) / 1000);

    const remainingTimeElement = document.getElementById('remainingTime');
    if (remainingTimeElement) {
      remainingTimeElement.innerHTML = `<span class="remaining-time-label">다음 마니또 배정 가능 시간까지</span><br>
  <span class="remaining-time-value">${hours}시간 ${minutes}분 ${seconds}초</span>`;
    }
  }
}

setInterval(updateRemainingTime, 1000);

window.ManitoPage = ManitoPage;
document.addEventListener('DOMContentLoaded', () => {
  ManitoPage.init();
});

document.addEventListener('DOMContentLoaded', () => {
  ManitoPage.init();
});


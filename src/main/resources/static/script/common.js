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
    const warningConfirmBtn = document.getElementById('warningConfirmBtn');

    if (warningPopup && warningMessage && warningConfirmBtn) {
      warningMessage.textContent = message;
      warningPopup.style.display = 'block';

      warningConfirmBtn.onclick = () => {
        warningPopup.style.display = 'none';
      };
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

// 공통 기능
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

  loadContent(page) {
    const middleSection = document.getElementById('middleSection');
    if (!middleSection) {
      return;
    }

    middleSection.innerHTML = '<div class="loading">불러오는 중...</div>';

    if (page === 'notification') {
      return;
    }

    const url = '/fragments/content/' + page;

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

      if (page === 'manito') {
        if (!document.querySelector('script[src="/script/manito.js"]')) {
          const scriptElement = document.createElement('script');
          scriptElement.src = '/script/manito.js';
          scriptElement.onload = () => {
            if (typeof ManitoPage !== 'undefined') {
              ManitoPage.init();
            }
          };
          document.body.appendChild(scriptElement);
        } else {
          if (typeof ManitoPage !== 'undefined') {
            ManitoPage.init();
          }
        }
      }

      this.initializePageModals();
      this.initializeAllTextareas();
    })
    .catch(error => {
      console.error('Error:', error);
      middleSection.innerHTML = '<div class="error">Failed to load content</div>';
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

  initializeNavigation() {
    const navButtons = document.querySelectorAll('.UI-icon-list button');
    navButtons.forEach(button => {
      button.addEventListener('click', () => {
        const buttonType = button.querySelector('img')?.alt;
        if (!buttonType) {
          return;
        }

        switch (buttonType) {
          case 'home':
            this.loadContent('timeline');
            break;
          case 'notification':
            this.loadContent('notification');
            break;
          case 'manito':
            this.loadContent('manito');
            break;
          case 'user-profile':
            this.loadContent('mypage');
            break;
        }
      });
    });

    const logoLink = document.querySelector('.home-link');
    if (logoLink) {
      logoLink.addEventListener('click', (e) => {
        e.preventDefault();
        this.loadContent('timeline');
      });
    }
  }
};

// 페이지 로드시 초기화
document.addEventListener('DOMContentLoaded', () => {
  CommonUtils.initializeNavigation();
  CommonUtils.initializePageModals();
  CommonUtils.initializeAllTextareas();
});
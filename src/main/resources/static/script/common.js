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
  }
};

document.addEventListener('DOMContentLoaded', () => {
  CommonUtils.initializePageModals();
  CommonUtils.initializeAllTextareas();

  window.toggleManito = function (element, type) {
    CommonUtils.toggleElement(element, type);
  };
});
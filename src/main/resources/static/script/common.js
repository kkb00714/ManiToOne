class BaseModal {
  constructor(modalId, backgroundId, openBtnId, closeBtnId) {
    this.modal = document.getElementById(modalId);
    this.background = document.getElementById(backgroundId);
    this.openBtn = document.getElementById(openBtnId);
    this.closeBtn = document.getElementById(closeBtnId);

    this.initializeEventListeners();
  }

  initializeEventListeners() {
    this.openBtn.onclick = () => this.open();
    this.closeBtn.onclick = () => this.close();
  }

  open() {
    [this.modal, this.background].forEach((el) => (el.style.display = "block"));
  }

  close() {
    [this.modal, this.background].forEach((el) => (el.style.display = "none"));
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

function loadContent(page) {
  const middleSection = document.getElementById('middleSection');
  middleSection.innerHTML = '<div class="loading">Loading...</div>';

  fetch(`/fragments/content/${page}`)
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
});


function toggleManito(element, type) {
  const img = element.querySelector('img');
  const isChecked = img.src.includes('icon-check.png');

  if (isChecked) {
    img.src = img.getAttribute('data-unchecked-src').replace('@{', '').replace('}', '');
    element.style.opacity = '0.3';
  } else {
    img.src = img.getAttribute('data-checked-src').replace('@{', '').replace('}', '');
    element.style.opacity = '1';
  }

  // 추후 백엔드 연동 시 활용할 수 있는 type 파라미터 ('response' 또는 'reply')
  // console.log(`${type} toggled to ${!isChecked}`);
}
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
    this.initializeEventListeners();
  }

  initializeEventListeners() {
    super.initializeEventListeners();

    // 신고 전송 버튼 이벤트
    const reportSendBtn = document.getElementById('reportSendBtn');
    if (reportSendBtn) {
      reportSendBtn.onclick = (e) => {
        e.preventDefault();
        this.handleReport();
      };
    }

    // 신고 타입 선택 이벤트
    const reportTypeSelect = document.getElementById('report-type-select');
    if (reportTypeSelect) {
      reportTypeSelect.onchange = (e) => {
        this.reportType = e.target.value;
      };
    }
  }

  async handleReport() {
    if (!this.targetId || !this.reportType) {
      this.showWarning('신고 사유를 선택해주세요.');
      return;
    }

    try {
      const response = await fetch(`/api/manito/report/${this.targetId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          reportType: this.reportType
        })
      });

      if (!response.ok) {
        throw new Error('신고 처리 중 오류가 발생했습니다.');
      }

      this.showWarning('신고가 접수되었습니다.');
      this.resetForm();
      this.close();
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
  }

  openWithTarget(targetId) {
    this.targetId = targetId;
    this.open();
  }
}

// 신고 메뉴 및 모달 관리를 위한 기능 추가
const initializeReportSystem = () => {
  const reportModal = new ReportModal();

  // 모든 more options 버튼에 대한 이벤트 리스너 설정
  document.addEventListener('click', (e) => {
    const moreOptionsBtn = e.target.closest('.tiny-icons[src*="UI-more2.png"]');
    if (moreOptionsBtn) {
      e.stopPropagation();

      // 기존에 열려있는 모든 신고 메뉴 닫기
      document.querySelectorAll('.manito-report-menu').forEach(menu => {
        menu.style.display = 'none';
      });

      // 클릭된 버튼의 신고 메뉴 표시
      const reportMenu = moreOptionsBtn.parentElement.querySelector('.manito-report-menu');
      if (reportMenu) {
        reportMenu.style.display = 'block';

        // 메뉴 위치 조정
        const rect = moreOptionsBtn.getBoundingClientRect();
        reportMenu.style.top = '100%';
        reportMenu.style.right = '0';
      }
    }
  });

  // 신고하기 버튼 클릭 이벤트
  document.addEventListener('click', (e) => {
    if (e.target.id === 'openReportModalBtn') {
      const letterContainer = e.target.closest('.manito-reply-outer-container');
      if (letterContainer) {
        const letterId = letterContainer.dataset.letterId;
        reportModal.openWithTarget(letterId);

        // 신고 메뉴 닫기
        const reportMenu = e.target.closest('.manito-report-menu');
        if (reportMenu) {
          reportMenu.style.display = 'none';
        }
      }
    }
  });

  // 다른 곳 클릭시 신고 메뉴 닫기
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.manito-report-menu') &&
        !e.target.closest('.tiny-icons[src*="UI-more2.png"]')) {
      document.querySelectorAll('.manito-report-menu').forEach(menu => {
        menu.style.display = 'none';
      });
    }
  });
};

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
  initializeReportSystem();
});
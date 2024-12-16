document.addEventListener("DOMContentLoaded", function () {
  const tableBody = document.querySelector("#manito-report-table-body");
  const pagination = document.querySelector(".pagination");
  const searchButton = document.querySelector("#searchButton");
  const searchQuery = document.querySelector("#searchQuery");
  const modalContainer = document.querySelector(".post-modal");
  const modalBackground = document.querySelector("#modalOverlay");
  const closeModalButton = document.querySelector("#closeProfileUpdateBtn");

  let requestBody = {}
  let currentPage = 1;

  function formatDatetimeSecond(input) {
    if (!input) {
      return "없음";
    }

    const [date, time] = input.split("T");
    return `${date} ${time}`;
  }

  function syncSearchFields() {
    const filterSelect = document.querySelector("#filterSelect");
    const searchQuery = document.querySelector("#searchQuery");

    const [key] = Object.keys(requestBody);
    if (key) {
      filterSelect.value = key;
      searchQuery.value = requestBody[key];
    } else {
      const defaultFilter = filterSelect.options[0].value;
      requestBody = {[defaultFilter]: ""};
    }
  }

  const handleSearchClick = () => {
    const filterSelect = document.querySelector("#filterSelect").value;
    const searchQuery = document.querySelector(
        "#searchQuery").value.trim();

    if (!searchQuery) {
      alert("검색어를 입력해 주세요.");
      return;
    }

    if (filterSelect && searchQuery) {
      requestBody = {[filterSelect]: searchQuery};
      loadPage(1);
    }
  }
  searchButton.addEventListener("click", handleSearchClick);

  loadPage(1);

  function loadPage(page) {
    currentPage = page;
    syncSearchFields();

    tableBody.innerHTML = '<tr><td colspan="8">Loading...</td></tr>';

    fetch(`/admin/manito/reports?page=${page - 1}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody)
    })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to fetch data");
      }
      return response.json();
    })
    .then((data) => {
      if (!data.content || data.content.length === 0) {
        tableBody.innerHTML = `
    <tr>
      <td colspan="8" class="text-center">데이터가 없습니다</td>
    </tr>
  `;
      } else {
        tableBody.innerHTML = data.content
        .map((report) => {
          return `
                <tr data-report='${JSON.stringify(report)}'>
                    <td>${report.reportId}</td>
                    <td data-value="${report.type.data}">${report.type.label}</td> 
                    <td data-value="${report.reportType.data}">${report.reportType.label
          > 5 ? report.reportType.label.substring(0, 5) + '...'
              : report.reportType.label}</td>
                    <td>${report.content.length > 6 ? report.content.substring(
              0, 6) + '...' : report.content}</td>
                    <td>${report.reportedByUser.nickname}</td> 
                    <td>
                        ${report.reportedToUser.nickname}
                    </td>
                    <td>${formatDatetimeSecond(report.createdAt)}</td>
            `;
        })
        .join("");
        renderPagination(page, data.totalPages);
      }
    })
    .catch((error) => {
      console.error("Error:", error);
    });
  }

  function renderPagination(currentPage, totalPages) {
    const maxVisiblePages = 3;
    const startPage = Math.floor((currentPage - 1) / maxVisiblePages)
        * maxVisiblePages + 1;
    const endPage = Math.min(startPage + maxVisiblePages - 1, totalPages);
    const previousGroupLastPage = startPage - 1;
    const nextGroupFirstPage = endPage + 1;

    let html = `

            <li>
                <a class="page-link ${currentPage <= 1 ? "disabled" : "active"}" data-page="1">
                    <img class="page-img"
           src="/images/admin/${currentPage === 1
        ? "previous_group_disabled.png"
        : "previous_group.png"}"
           alt="처음으로" />
                    </a>
            </li>
            <li>
                <a class="page-link ${currentPage <= maxVisiblePages
        ? "disabled"
        : "active"}" data-page="${previousGroupLastPage}">
                    <img class="page-img"
           src="/images/admin/${currentPage <= maxVisiblePages
        ? "previous_disabled.png" : "previous.png"}"
           alt="이전 그룹" />
                    </a>
            </li>
        `;

    for (let i = startPage; i <= endPage; i++) {
      html += `
                <li>
                    <a class="page-link-number ${i === currentPage ? "disabled"
          : "active"}" data-page="${i}">
                        ${i}</a>
                </li>
            `;
    }

    html += `

            <li>
                <a class="page-link ${endPage === totalPages ? "disabled"
        : "active"}" data-page="${nextGroupFirstPage}">
                    <img class="page-img"
           src="/images/admin/${endPage === totalPages ? "next_disabled.png"
        : "next.png"}"
           alt="다음 그룹" />
                    </a>
            </li>
            <li>
                <a class="page-link ${currentPage === totalPages ? "disabled"
        : "active"}" data-page="${totalPages}">
                    <img class="page-img"
           src="/images/admin/${currentPage === totalPages
        ? "next_group_disabled.png" : "next_group.png"}"
           alt="마지막으로" />
                    </a>
            </li>
        `;

    pagination.innerHTML = html;

    document.querySelectorAll(".page-link, .page-link-number").forEach(
        (button) => {
          if (!button.classList.contains("disabled")) {
            button.addEventListener("click", () => {
              const page = parseInt(button.dataset.page, 10);
              loadPage(page);
            });
          }
        });
  }

  searchQuery.addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
      event.preventDefault();
      searchButton.click();
    }
  });

  // 마니또 답변 , 감사인사 필터
  let typeLinkItems = document.querySelectorAll('.type-filter');

  typeLinkItems.forEach((link) => {
    link.addEventListener("click", function (event) {
      event.preventDefault();

      document.querySelectorAll(".type-filter").forEach(
          (link) => link.classList.remove("active"));
      this.classList.add("active");
      const currentStatus = this.dataset.status;
      if (currentStatus !== "") {
        requestBody.type = this.dataset.status;
      } else {
        requestBody.type = null;
      }
      loadPage(1);
    });
  });

  // 신고 사유 필터
  const reportTypeSelect = document.querySelector("#reportTypeSelect");

  reportTypeSelect.addEventListener("change", function () {
    const selectedValue = this.value;

    if (selectedValue === "") {
      requestBody.reportType = null;
    } else {
      requestBody.reportType = selectedValue;
    }

    loadPage(1);
  });

  function initializeSlider(reportData, imageUrls) {
    // DOM 요소 가져오기
    const imageSlider = document.querySelector(".image-slider");
    const imageContainer = document.getElementById("imageContainer");
    const prevBtn = document.getElementById("prevBtn");
    const nextBtn = document.getElementById("nextBtn");

    // 이미지 초기화
    imageContainer.innerHTML = ""; // 이전 이미지 초기화

    // 이미지 추가
    imageUrls.forEach((url) => {
      const img = document.createElement("img");
      img.src = url;
      img.alt = "Slider Image";
      imageContainer.appendChild(img);
    });

    // 이미지가 없을 경우 슬라이더 숨김
    if (imageUrls.length === 0) {
      imageSlider.style.display = "none";
      return; // 더 이상 진행하지 않음
    } else {
      imageSlider.style.display = "block"; // 슬라이더 표시
    }

    // 슬라이더 초기 상태 설정
    let currentIndex = 0; // 초기화 위치를 위로 이동

    // 버튼 클릭 이벤트
    prevBtn.addEventListener("click", () => {
      if (currentIndex > 0) {
        currentIndex--;
        updateSlider();
      }
    });

    nextBtn.addEventListener("click", () => {
      if (currentIndex < imageUrls.length - 1) {
        currentIndex++;
        updateSlider();
      }
    });

    // 슬라이더 업데이트 함수
    function updateSlider() {
      const offset = -currentIndex * imageSlider.offsetWidth;
      imageContainer.style.transform = `translateX(${offset}px)`;
      updateButtons();
    }

    // 버튼 상태 업데이트
    function updateButtons() {
      prevBtn.style.display = currentIndex > 0 ? "block" : "none";
      nextBtn.style.display = currentIndex < imageUrls.length - 1 ? "block"
          : "none";
    }

    // 초기 버튼 상태 및 슬라이더 위치 설정
    updateButtons();
    updateSlider();
  }

  function openProfileModal(reportData, imageData) {
    // 첫 번째 데이터 - postData
    const profileImageElement = document.querySelector(".profile-image");
    const userIdElement = document.querySelector(".user-id");
    const postTimeElement = document.querySelector(".post-time");
    const postContentElement = document.querySelector(".post-content");

    const replyProfileImageElement = document.querySelector(
        ".reply-profile-image");
    const replyUserIdElement = document.querySelector(".reply-user-id");
    const replyContentElement = document.querySelector(".reply-content");
    const replyHeader = document.querySelector(".reply-header");

    profileImageElement.src = reportData.post.user.profileImage
        || "/images/icons/UI-user2.png";
    userIdElement.textContent = reportData.post.user.nickname;
    postContentElement.textContent = reportData.post.content;
    postTimeElement.textContent = reportData.post.timeDifference;

    if (reportData.type.data === "MANITO_LETTER") {
      replyHeader.textContent = "신고된 편지";
    } else if (reportData.type.data === "MANITO_ANSWER") {
      replyHeader.textContent = "신고된 답변";
    }
    replyProfileImageElement.src = reportData.reportedToUser.profileImage || "/images/icons/UI-user2.png";
    replyUserIdElement.textContent = reportData.reportedToUser.nickname;
    replyContentElement.textContent = reportData.content;

    const imageUrls = imageData.map(image => image.fileName);

    // 이미지 슬라이더 초기화
    initializeSlider(reportData, imageUrls);

    modalContainer.style.display = "block";
    modalBackground.style.display = "block";

    closeModalButton.addEventListener("click", function () {
      modalContainer.style.display = "none";
      modalBackground.style.display = "none";
    });

    modalBackground.addEventListener("click", function (event) {
      if (event.target === modalBackground) {
        modalContainer.style.display = "none";
        modalBackground.style.display = "none";
      }
    });
  }

  tableBody.addEventListener("click", function (event) {
    const row = event.target.closest("tr");
    if (row) {
      const reportData = JSON.parse(row.dataset.report);  // 이미 바인딩된 데이터
      console.log(reportData);

      // type에 따라 postId 설정
      let postId = reportData.post.postId;

      fetch(`/admin/post/${postId}/image`)
      .then(response => response.json())
      .then(imageData => {
        console.log("성공");
        console.log(imageData);
        openProfileModal(reportData, imageData);
      })
      .catch(error => {
        console.error("이미지 로드 오류:", error);
      });
    }
  });
});

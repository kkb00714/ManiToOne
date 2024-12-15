document.addEventListener("DOMContentLoaded", function () {
  const tableBody = document.querySelector("#post-table-body");
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

    tableBody.innerHTML = '<tr><td colspan="9">Loading...</td></tr>';

    fetch(`/admin/posts?page=${page - 1}`, {
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
      tableBody.innerHTML = data.content
      .map(
          (post) => `
<tr data-post='${JSON.stringify(post)}'>
          <td>${post.postId}</td>
          <td>${post.user.name}</td>
          <td>${post.user.nickname}</td>
          <td>${post.user.email}</td>
          <td>${post.content.length > 15 ? post.content.substring(0, 15) + '...'
              : post.content}</td>
          <td>${formatDatetimeSecond(post.createdAt)}</td>
          <td class="blind-status">${post.isBlind ? 'O' : 'X'}</td>
          <td><a href="#" class="change-status" data-id="${post.postId}">변경</a></td>
          <td><a href="#" class="delete-post" data-id="${post.postId}">삭제</a></td>
        </tr>
                      `
      )
      .join("");

      renderPagination(page, data.totalPages);
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

  // 엔터 키를 눌렀을 때 검색 버튼 클릭
  searchQuery.addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
      event.preventDefault(); // 기본 동작 방지
      searchButton.click();   // 검색 버튼 클릭 트리거
    }
  });

  let statusLinkItems = document.querySelectorAll('.status-filter');

  statusLinkItems.forEach((link) => {
    link.addEventListener("click", function (event) {
      event.preventDefault();

      document.querySelectorAll(".status-filter").forEach(
          (link) => link.classList.remove("active"));
      this.classList.add("active");

      const currentStatus = this.dataset.status;

      if (currentStatus === "0") {
        requestBody.isBlind = false;
      } else if (currentStatus === "1") {
        requestBody.isBlind = true;
      } else {
        requestBody.isBlind = null;
      }

      loadPage(1);
    });
  });

  // 게시글 블라인드 처리
  document.addEventListener("click", function (event) {
    if (event.target.classList.contains("change-status")) {
      event.preventDefault();

      const postId = event.target.dataset.id;
      const currentRow = event.target.closest('tr');
      const blindStatusCell = currentRow.querySelector('.blind-status');

      fetch(`/admin/blind/post/${postId}`, {
        method: "PUT",
      })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch data");
        }
        return response.json();
      })
      .then((data) => {
        blindStatusCell.textContent = data.isBlind ? 'O' : 'X';
      })
      .catch((error) => {
        console.error("Error:", error);
      });
    }
  });

  // 게시글 삭제 처리
  document.addEventListener("click", function (event) {
    if (event.target.classList.contains("delete-post")) {
      event.preventDefault();

      const postId = event.target.dataset.id;

      if (confirm("정말로 이 게시글을 삭제하시겠습니까?")) {
        fetch(`/admin/report/post/${postId}`, {
          method: "GET",
        })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to delete post");
          }
          return response.json();
        })
        .then((isReportedPost) => {
          if (isReportedPost) {
            if (confirm("해당 게시글은 신고된 게시글입니다. 정말 삭제하시겠습니까? (신고 목록도 삭제)")) {
              deletePost(postId);
            }
          } else {
            deletePost(postId);
          }
        })
        .catch((error) => {
          console.error("Error:", error);
          alert("게시글 삭제 중 오류가 발생했습니다.");
        });
      }
    }
  });

  function deletePost(postId) {
    fetch(`/admin/post/${postId}`, {
      method: "DELETE",
    })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to delete post");
      }
      return response.text();
    })
    .then(() => {
      loadPage(currentPage); // 현재 페이지 새로 로드
    })
    .catch((error) => {
      console.error("Error:", error);
      alert("게시글 삭제 중 오류가 발생했습니다.");
    });
  }

  function initializeSlider(postData, imageUrls) {
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
      nextBtn.style.display = currentIndex < imageUrls.length - 1 ? "block" : "none";
    }

    // 초기 버튼 상태 및 슬라이더 위치 설정
    updateButtons();
    updateSlider();
  }

  function openProfileModal(postData, imageData) {
    // 첫 번째 데이터 - postData
    const profileImageElement = document.querySelector(".profile-image");
    const userIdElement = document.querySelector(".user-id");
    const postTimeElement = document.querySelector(".post-time");
    const postContentElement = document.querySelector(".post-content");

    profileImageElement.src = postData.user.profileImage || "/images/icons/UI-user2.png";
    userIdElement.textContent = postData.user.name;
    postTimeElement.textContent = postData.timeDifference;
    postContentElement.textContent = postData.content;

    const imageUrls = imageData.map(image => image.fileName);

    // 이미지 슬라이더 초기화
    initializeSlider(postData, imageUrls);

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
      const postData = JSON.parse(row.dataset.post);  // 이미 바인딩된 데이터
      const postId = postData.postId;

      fetch(`/admin/post/${postId}/image`)
      .then(response => response.json())
      .then(imageData => {
        openProfileModal(postData, imageData);
      })
      .catch(error => {
        console.error("이미지 로드 오류:", error);
      });
    }
  });
});
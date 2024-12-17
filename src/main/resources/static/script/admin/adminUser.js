document.addEventListener("DOMContentLoaded", function () {
  const tableBody = document.querySelector("#user-table-body");
  const pagination = document.querySelector(".pagination");
  const searchButton = document.querySelector("#searchButton");
  const searchQuery = document.querySelector("#searchQuery");
  const modalContainer = document.querySelector("#profileUpdateModalContainer");
  const modalBackground = document.querySelector("#profileUpdateModalBackground");
  const closeModalButton = document.querySelector("#closeProfileUpdateBtn");
  let requestBody = {}
  let currentStatus = "";

  function formatDatetime(input) {
    if (!input) return "없음";

    const [date, time] = input.split("T");
    const [hours, minutes] = time.split(":");
    return `${date} ${hours}:${minutes}`;
  }

  function formatDatetimeSecond(input) {
    if (!input) return "없음";

    const [date, time] = input.split("T");
    return `${date} ${time}`;
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
    syncSearchFields();

    tableBody.innerHTML = '<tr><td colspan="8">Loading...</td></tr>';

    fetch(`/admin/users?page=${page - 1}`, {
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
      console.log(data)
      tableBody.innerHTML = data.content
      .map(
          (user) => `
<tr data-user='${JSON.stringify(user)}'>
          <td>${user.userId}</td>
          <td>${user.name}</td>
          <td>${user.nickname}</td>
          <td>${user.email}</td>
          <td>${user.birth}</td>
          <td>${user.role}</td>
          <td>${formatDatetimeSecond(user.createdAt)}</td>
          <td>${formatDatetime(user.unbannedAt)}</td>
          <td>${getStatusText(user.status)}</td>
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

  searchQuery.addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
      event.preventDefault();
      searchButton.click();
    }
  });

  let statusLinkItems = document.querySelectorAll('.status-filter');

  statusLinkItems.forEach((link) => {
    link.addEventListener("click", function (event) {
      event.preventDefault();

      document.querySelectorAll(".status-filter").forEach((link) => link.classList.remove("active"));
      this.classList.add("active");

      currentStatus = this.dataset.status;
      requestBody.status = currentStatus || null;

      loadPage(1);
    });
  });

  function getStatusText(status) {
    switch (status) {
      case 1:
        return "활동";
      case 2:
        return "정지";
      case 3:
        return "탈퇴";
      default:
        return "알 수 없음";
    }
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

  let originalData = {}
  let clickedRow = null;

  tableBody.addEventListener("click", function (event) {
    const row = event.target.closest("tr");
    if (row) {
      const userData = JSON.parse(row.dataset.user);
      clickedRow = row;
      userData.status = parseInt(userData.status, 10);
      originalData = userData;
      openProfileModal(userData);
    }
  });

  function formatToDatetimeLocal(input) {
    if (!input) return null;

    const [date, time] = input.split("T");
    const [hours, minutes] = time.split(":");
    return `${date}T${hours}:${minutes}`;
  }

  function openProfileModal(userData) {
    document.querySelector("#user-name").value = userData.name || "";
    document.querySelector("#user-nickname").value = userData.nickname || "";
    document.querySelector("#user-introduce").value = userData.introduce || "";
    document.querySelector("#user-email").value = userData.email || "";
    document.querySelector("#user-birth").value = userData.birth || "";
    document.querySelector("#user-role").value = userData.role || "";
    document.querySelector("#user-unbanned").value = formatToDatetimeLocal(userData.unbannedAt);
    document.querySelector("#user-status").value = userData.status;
    document.querySelector(".user-photo").src = userData.profileImage;

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

  document.querySelector("#clearDateButton").addEventListener("click", function (e) {
    e.preventDefault();
    const dateInput = document.querySelector("#user-unbanned");
    dateInput.value = "";
  });

  function getFormData(form) {
    const formData = new FormData(form);
    const data = {};
    formData.forEach((value, key) => {
      if (key === "status") {
        data[key] = value ? parseInt(value, 10) : null;
      } else {
        data[key] = value === "" ? null : value;
      }
    });
    return data;
  }

  function getChangedData(original, current) {
    const changedData = {};
    for (const key in current) {
      let originalValue = original[key];
      let currentValue = current[key];

      if (key === "unbannedAt") {
        originalValue = removeSecondsFromDatetime(originalValue);
        currentValue = removeSecondsFromDatetime(currentValue);
        if (originalValue === currentValue) {
          continue;
        } else {
          if (currentValue === null) {
            changedData["clearUnbannedAt"] = true;
          } else {
            changedData["clearUnbannedAt"] = false;
          }
        }
      }

      if (current[key] !== original[key]) {
        changedData[key] = current[key];
      }
    }
    return changedData;
  }

  function removeSecondsFromDatetime(input) {
    if (!input) return input;
    const [date, time] = input.split("T");
    const [hours, minutes] = time.split(":");
    return `${date}T${hours}:${minutes}`;
  }

  const form = document.querySelector("#profile_form");
  const saveButton = document.querySelector("#update_profile_button");

  saveButton.addEventListener("click", (e) => {
    e.preventDefault();
    const currentData = getFormData(form);
    let changedData = getChangedData(originalData, currentData);

    if (Object.keys(changedData).length === 0) {
      alert("변경된 값이 없습니다.");
      return;
    }

    changedData["userId"] = originalData["userId"];

    fetch("/admin/users", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(changedData),
    })
    .then((response) => {
      if (!response.ok) {
        return response.text().then((message) => {
          throw new Error(message);
        });
      }
      return response.json();
    })
    .then((updatedUser) => {
      clickedRow.dataset.user = JSON.stringify(updatedUser);
      clickedRow.innerHTML = `
          <td>${updatedUser.userId}</td>
          <td>${updatedUser.name}</td>
          <td>${updatedUser.nickname}</td>
          <td>${updatedUser.email}</td>
          <td>${updatedUser.birth}</td>
          <td>${updatedUser.role}</td>
          <td>${formatDatetimeSecond(user.createdAt)}</td>
          <td>${formatDatetime(updatedUser.unbannedAt)}</td>
          <td>${getStatusText(updatedUser.status)}</td>
      `;

      modalContainer.style.display = "none";
      modalBackground.style.display = "none";
    })
    .catch((error) => {
      alert(`${error.message}`);
    });
  });

  const profileImage = document.querySelector("#user-photo");
  const profileImageInput = document.querySelector("#profile-image-input");
  const modal = document.querySelector("#image-action-modal");
  const deletePhotoBtn = document.querySelector("#delete-photo-btn");
  const uploadPhotoBtn = document.querySelector("#upload-photo-btn");
  const closeModalBtn = document.querySelector("#close-modal-btn");
  const modalOverlay = document.querySelector("#modal-overlay");

  function updateProfileImage(file) {
    const formData = new FormData();
    formData.append("profileImageFile", file);

    const userId = originalData.userId;

    fetch(`/admin/users/${userId}`, {
      method: "PUT",
      body: formData,
    })
    .then((response) => {
      if (!response.ok) {
        return response.text().then((message) => {
          throw new Error(message);
        });
      }
      return response.json();
    })
    .then((updatedUser) => {
      alert("프로필 이미지가 성공적으로 업데이트되었습니다.");
      profileImage.src = updatedUser.profileImage;
    })
    .catch((error) => {
      alert(`프로필 이미지 업데이트에 실패했습니다: ${error.message}`);
    });
  }

  profileImage.addEventListener("click", () => {
    modal.style.display = "block";
    modalOverlay.style.display = "block";
  });

  const closeModal = () => {
    modal.style.display = "none";
    modalOverlay.style.display = "none";
  };

  closeModalBtn.addEventListener("click", closeModal);

  modalOverlay.addEventListener("click", closeModal);

  deletePhotoBtn.addEventListener("click", () => {
    // profileImage.src = defaultImageSrc;
    // profileImageInput.value = "";
    updateProfileImage(null);
    closeModal();
  });

  uploadPhotoBtn.addEventListener("click", () => {
    profileImageInput.click();
    closeModal();
  });

  profileImageInput.addEventListener("change", (event) => {
    const file = event.target.files[0];
    if (file) {
      if (!file.type.match(/^image\/(png|jpe?g)$/)) {
        alert("PNG, JPG, JPEG 파일만 선택할 수 있습니다.");
        profileImageInput.value = "";
        return;
      }
      updateProfileImage(file);
    }
  });
});
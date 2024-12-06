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

  loadPage(1);

  function loadPage(page) {
    searchButton.removeEventListener("click", handleSearchClick);
    searchButton.addEventListener("click", handleSearchClick);
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

    searchQuery.addEventListener("keydown", function (event) {
      if (event.key === "Enter") {
        event.preventDefault();
        searchButton.click();
      }
    });

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

  tableBody.addEventListener("click", function (event) {
    const row = event.target.closest("tr");
    if (row) {
      const userData = JSON.parse(row.dataset.user);
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
    const changedData = getChangedData(originalData, currentData);

    if (Object.keys(changedData).length === 0) {
      alert("변경된 값이 없습니다.");
      return;
    }

  });
});
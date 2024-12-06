document.addEventListener("DOMContentLoaded", function () {
  const tableBody = document.querySelector("#user-table-body");
  const pagination = document.querySelector(".pagination");
  let requestBody = {}

  loadPage(1);

  function loadPage(page) {
    syncSearchFields();
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
                        <tr>
                            <td>${user.userId}</td>
                            <td>${user.name}</td>
                            <td>${user.nickname}</td>
                            <td>${user.email}</td>
                            <td>${user.birth}</td>
                            <td>${user.role}</td>
                            <td>${user.unbannedAt ? user.unbannedAt : '없음'}</td>
                            <td>${getStatusText(user.status)}</td>
                        </tr>`
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
    const startPage = Math.floor((currentPage - 1) / maxVisiblePages) * maxVisiblePages + 1;
    const endPage = Math.min(startPage + maxVisiblePages - 1, totalPages);
    const previousGroupLastPage = startPage - 1;
    const nextGroupFirstPage = endPage + 1;

    let html = `

            <li>
                <a class="page-link ${currentPage <= 1 ? "disabled" : "active"}" data-page="1">
                    <img class="page-img"
           src="/images/admin/${currentPage === 1 ? "previous_group_disabled.png"
        : "previous_group.png"}"
           alt="처음으로" />
                    </a>
            </li>
            <li>
                <a class="page-link ${currentPage <= maxVisiblePages ? "disabled"
        : "active"}" data-page="${previousGroupLastPage}">
                    <img class="page-img"
           src="/images/admin/${currentPage <= maxVisiblePages ? "previous_disabled.png" : "previous.png"}"
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
           src="/images/admin/${endPage === totalPages ? "next_disabled.png" : "next.png"}"
           alt="다음 그룹" />
                    </a>
            </li>
            <li>
                <a class="page-link ${currentPage === totalPages ? "disabled"
        : "active"}" data-page="${totalPages}">
                    <img class="page-img"
           src="/images/admin/${currentPage === totalPages ? "next_group_disabled.png" : "next_group.png"}"
           alt="마지막으로" />
                    </a>
            </li>
        `;

    pagination.innerHTML = html;

    document.querySelector("form").addEventListener("submit", function (e) {
      e.preventDefault();

      const filterSelect = document.querySelector("#filterSelect").value;
      const searchQuery = document.querySelector("#searchQuery").value.trim();

      if (filterSelect && searchQuery) {
        requestBody = { [filterSelect]: searchQuery };
        loadPage(1);
      }
    });

    document.querySelectorAll(".page-link, .page-link-number").forEach((button) => {
      if (!button.classList.contains("disabled")) {
        button.addEventListener("click", () => {
          const page = parseInt(button.dataset.page, 10);
          loadPage(page);
        });
      }
    });
  }

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
      requestBody = { [defaultFilter]: "" };
    }
  }
});
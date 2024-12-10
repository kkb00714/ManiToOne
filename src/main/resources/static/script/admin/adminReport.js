document.addEventListener("DOMContentLoaded", function () {
  const tableBody = document.querySelector("#post-table-body");
  const pagination = document.querySelector(".pagination");
  const searchButton = document.querySelector("#searchButton");
  const searchQuery = document.querySelector("#searchQuery");

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

  loadPage(1);

  function loadPage(page) {
    currentPage = page;
    searchButton.removeEventListener("click", handleSearchClick);
    searchButton.addEventListener("click", handleSearchClick);
    syncSearchFields();

    tableBody.innerHTML = '<tr><td colspan="8">Loading...</td></tr>';

    fetch(`/admin/reports?page=${page - 1}`, {
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
      .map((report) =>
          `<tr data-user='${JSON.stringify(report)}'>
            <td>${report.reportId}</td> <!-- No -->
            <td data-value="${report.type.data}">${report.type.label}</td> <!-- 신고 유형 -->
            <td data-value="${report.reportType.data}">${report.reportType.label}</td> <!-- 신고 사유 -->
            <td>${report.content.length > 15 ? report.content.substring(0, 15) + '...'
                    : report.content}</td>
            <td>${report.reportedByUser.nickname}</td> <!-- 신고한 닉네임 -->
          <!-- 신고 당한 닉네임 -->
            <td>
              ${report.post ? report.post.user.nickname : ''}
              ${report.replyPost ? report.replyPost.user.nickname : ''}
            </td>
            <td>${formatDatetimeSecond(report.createdAt)}</td>
          <!-- 블라인드 여부 -->
            <td class="blind-status">
              ${report.post ? (report.post.isBlind ? 'O' : 'X') : ''}
              ${report.replyPost ? (report.replyPost.isBlind ? 'O' : 'X') : ''}
            </td>
          <!-- 상태 변경 -->
            <td><a href="#" class="change-status" data-id="${report.post
                    ? report.post.postId : report.replyPost.replyPostId}">변경</a></td>
          <!-- 삭제 -->
            <td><a href="#" class="delete-post" data-id="${report.post
                    ? report.post.postId : report.replyPost.replyPostId}">삭제</a></td>
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
});
document.addEventListener("DOMContentLoaded", function () {
  const tableBody = document.querySelector("#user-table-body");
  const pagination = document.querySelector(".pagination"); // 페이징 컨테이너

  // 첫 번째 페이지 데이터 로드
  loadPage(1);

  function loadPage(page) {
    const requestBody = {};

    fetch(`/admin/users?page=${page - 1}`, {
      method: "POST", // POST 방식
      headers: {
        "Content-Type": "application/json", // JSON 형식으로 데이터 전송
      },
      body: JSON.stringify(requestBody) // 요청 본문에 JSON 데이터를 포함
    })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to fetch data");
      }
      return response.json(); // 응답 데이터를 JSON으로 변환
    })
    .then((data) => {
      console.log(data); // 서버에서 받은 JSON 데이터를 확인
      console.log(data.content)
      console.log(data.content[0])
      console.log(data.content[0].userId)
      // 테이블 업데이트
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

      renderPagination(page, data.totalPages); // totalPages를 서버 응답에서 가져올 경우 사용
    })
    .catch((error) => {
      console.error("Error:", error); // 에러를 콘솔에 출력
    });
  }

  function renderPagination(currentPage, totalPages) {
    const maxVisiblePages = 3;
    const startPage = Math.floor((currentPage - 1) / maxVisiblePages) * maxVisiblePages + 1;
    const endPage = Math.min(startPage + maxVisiblePages - 1, totalPages);
    const previousGroupLastPage = startPage - 1;
    const nextGroupFirstPage = endPage + 1;

    let html = `
            <!-- 이전 그룹 버튼 -->
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
            <!-- 다음 그룹 버튼 -->
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

    // 페이징 버튼 이벤트 바인딩
    document.querySelectorAll(".page-link, .page-link-number").forEach((button) => {
      if (!button.classList.contains("disabled")) {
        button.addEventListener("click", () => {
          const page = parseInt(button.dataset.page, 10);
          loadPage(page); // 선택한 페이지 데이터 로드
        });
      }
    });
  }

  // Status 값을 텍스트로 변환하는 함수
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
});
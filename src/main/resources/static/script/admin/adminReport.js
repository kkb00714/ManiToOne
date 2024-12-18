document.addEventListener("DOMContentLoaded", function () {
  const tableBody = document.querySelector("#report-table-body");
  const pagination = document.querySelector(".pagination");
  const searchButton = document.querySelector("#searchButton");
  const searchQuery = document.querySelector("#searchQuery");
  const modalContainer = document.querySelector(".post-modal");
  const modalBackground = document.querySelector("#modalOverlay");
  const closeModalButton = document.querySelector("#closeProfileUpdateBtn");

  let requestBody = {}
  let currentPage = 1;

  const toEnumValue = (filter) => {
    switch (filter) {
      case "reportedBy":
        return "REPORTED_BY";
      case "reportedTo":
        return "REPORTED_TO";
      case "content":
        return "CONTENT";
      default:
        return null;
    }
  };

  function formatDatetimeSecond(input) {
    if (!input) {
      return "없음";
    }

    const [date, time] = input.split("T");
    return `${date} ${time}`;
  }

  // function syncSearchFields() {
  //   const filterSelect = document.querySelector("#filterSelect");
  //   const searchQuery = document.querySelector("#searchQuery");
  //
  //   const [key] = Object.keys(requestBody);
  //   if (key) {
  //     filterSelect.value = key;
  //     searchQuery.value = requestBody[key];
  //   } else {
  //     const defaultFilter = filterSelect.options[0].value;
  //     requestBody = {[defaultFilter]: ""};
  //   }
  // }

  const handleSearchClick = () => {
    const filterSelect = document.querySelector("#filterSelect").value;
    const searchQuery = document.querySelector(
        "#searchQuery").value.trim();

    if (!searchQuery) {
      alert("검색어를 입력해 주세요.");
      return;
    }

    if (filterSelect && searchQuery) {
      const type = toEnumValue(filterSelect);
      const content = searchQuery;
      requestBody['type'] = type;
      requestBody['content'] = content;
      loadPage(1);
    }
  }
  searchButton.addEventListener("click", handleSearchClick);
  loadPage(1);

  function loadPage(page) {
    currentPage = page;
    // syncSearchFields();

    tableBody.innerHTML = '<tr><td colspan="11">Loading...</td></tr>';

    const params = new URLSearchParams({
      page: page - 1,
      type: requestBody.type || "",
      content: requestBody.content || "",
      reportObjectType: requestBody.reportObjectType || "",
      reportType: requestBody.reportType || "",
    });

    fetch(`/admin/api/reports?${params.toString()}`, {
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
      <td colspan="11" class="text-center">데이터가 없습니다</td>
    </tr>
  `;
      } else {
        tableBody.innerHTML = data.content
        .map((report) => {
          if (!report.post && !report.replyPost) {
            return `
                <tr data-report='${JSON.stringify(report)}'>
                    <td>${report.reportId}</td>
                    <td colspan="9" class="text-center">
                      삭제된 ${report.type ? report.type.label : ''}
                    </td>
                    <td><a href="#" class="delete-report" data-id="${report.reportId}">삭제</a></td>
                </tr>
            `;
          } else {
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
                        ${report.post ? report.post.user.nickname : ''}
                        ${report.replyPost ? report.replyPost.user.nickname
                : ''}
                    </td>
                    <td>${formatDatetimeSecond(report.createdAt)}</td>
                    <td class="blind-status">
                        ${report.post ? (report.post.isBlind ? 'O' : 'X') : ''}
                        ${report.replyPost ? (report.replyPost.isBlind ? 'O'
                : 'X') : ''}
                    </td>
                    <td><a href="#" class="change-status" data-id="${report.post
                ? report.post.postId : report.replyPost.replyPostId}"
                    data-value="${report.type.data}">변경</a></td>
                    <td><a href="#" class="delete-post" data-id="${report.post
                ? report.post.postId : report.replyPost.replyPostId}"
                    data-value="${report.type.data}">삭제</a></td>
                    <td><a href="#" class="delete-report" data-id="${report.reportId}">삭제</a></td>
                </tr>
            `;
          }
        })
        .join("");
      }
      renderPagination(page, data.totalPages);
    })
    .catch((error) => {
      console.error("Error:", error);
    });
  }

  function renderPagination(currentPage, totalPages) {
    // 데이터가 없을 경우 페이지네이션 숨김 처리
    if (!totalPages || totalPages === 0) {
      pagination.innerHTML = ""; // 페이지네이션 영역 비우기
      return;
    }

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

  // 게시글/댓글 삭제 처리
  // document.addEventListener("click", function (event) {
  //   if (event.target.classList.contains("delete-post")) {
  //     event.preventDefault();
  //
  //     const postOrReplyId = event.target.dataset.id;
  //     const postOrReply = event.target.dataset.value;
  //
  //     if (confirm("정말로 이 게시글/댓글을 삭제하시겠습니까?")) {
  //       if (postOrReply === "POST") {
  //         fetch(`/admin/report/post/${postOrReplyId}`, {
  //           method: "GET",
  //         })
  //         .then((response) => {
  //           if (!response.ok) {
  //             throw new Error("Failed to delete post");
  //           }
  //           return response.json();
  //         })
  //         .then((isReportedPost) => {
  //           if (isReportedPost) {
  //             if (confirm("해당 게시글/댓글은 신고된 게시글입니다. 정말 삭제하시겠습니까? (신고 목록도 삭제)")) {
  //               deletePost(postOrReplyId);
  //             }
  //           } else {
  //             deletePost(postOrReplyId);
  //           }
  //         })
  //         .catch((error) => {
  //           console.error("Error:", error);
  //           alert("게시글 삭제 중 오류가 발생했습니다.");
  //         });
  //       } else {
  //         fetch(`/admin/report/reply/${postOrReplyId}`, {
  //           method: "GET",
  //         })
  //         .then((response) => {
  //           if (!response.ok) {
  //             throw new Error("Failed to delete post");
  //           }
  //           return response.json();
  //         })
  //         .then((isReportedPost) => {
  //           if (isReportedPost) {
  //             if (confirm("해당 게시글/댓글은 신고된 게시글입니다. 정말 삭제하시겠습니까? (신고 목록도 삭제)")) {
  //               deleteReply(postOrReplyId);
  //             }
  //           } else {
  //             deleteReply(postOrReplyId);
  //           }
  //         })
  //         .catch((error) => {
  //           console.error("Error:", error);
  //           alert("게시글 삭제 중 오류가 발생했습니다.");
  //         });
  //       }
  //
  //     }
  //   }
  // });

  function deletePost(postId) {
    fetch(`/admin/api/post/${postId}`, {
      method: "DELETE",
    })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to delete post");
      }
      return response.text();
    })
    .then(() => {
      loadPage(currentPage);
    })
    .catch((error) => {
      console.error("Error:", error);
      alert("게시글 삭제 중 오류가 발생했습니다.");
    });
  }

  function deleteReply(replyId) {
    fetch(`/admin/api/reply/${replyId}`, {
      method: "DELETE",
    })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to delete post");
      }
      return response.text();
    })
    .then(() => {
      loadPage(currentPage);
    })
    .catch((error) => {
      console.error("Error:", error);
      alert("게시글 삭제 중 오류가 발생했습니다.");
    });
  }

  // 게시글/댓글 블라인드 처리
  // document.addEventListener("click", function (event) {
  //   if (event.target.classList.contains("change-status")) {
  //     event.preventDefault();
  //
  //     const postOrReplyId = event.target.dataset.id;
  //     const currentRow = event.target.closest('tr');
  //     const blindStatusCell = currentRow.querySelector('.blind-status');
  //     const postOrReply = event.target.dataset.value;
  //     let url = "";
  //     if (postOrReply === "POST") {
  //       url = `/admin/blind/post/${postOrReplyId}`;
  //     } else {
  //       url = `/admin/blind/reply/${postOrReplyId}`;
  //     }
  //     fetch(url, {
  //       method: "PUT",
  //     })
  //     .then((response) => {
  //       if (!response.ok) {
  //         throw new Error("Failed to fetch data");
  //       }
  //       return response.json();
  //     })
  //     .then((data) => {
  //       blindStatusCell.textContent = data.isBlind ? 'O' : 'X';
  //
  //       // 동일한 게시글 신고 항목 블라인드 상태 업데이트
  //       document.querySelectorAll("tr").forEach((row) => {
  //         const rowType = row.querySelector("[data-value]")?.dataset.value;
  //         const rowId = row.querySelector(".change-status")?.dataset.id;
  //
  //         if (rowType === postOrReply && rowId === postOrReplyId) {
  //           const blindCell = row.querySelector(".blind-status");
  //           if (blindCell) {
  //             blindCell.textContent = data.isBlind ? "O" : "X";
  //           }
  //         }
  //       });
  //     })
  //     .catch((error) => {
  //       console.error("Error:", error);
  //     });
  //   }
  // });

  // 신고 목록 삭제
  // document.addEventListener("click", function (event) {
  //   if (event.target.classList.contains("delete-report")) {
  //     if (confirm("정말로 신고를 삭제하시겠습니까? (관련된 신고목록 전부 삭제)")) {
  //       event.preventDefault();
  //
  //       const reportId = event.target.dataset.id;
  //
  //       fetch(`/admin/report/${reportId}`, {
  //         method: "DELETE",
  //       })
  //       .then((response) => {
  //         if (!response.ok) {
  //           throw new Error("Failed to delete post");
  //         }
  //         return response.text();
  //       })
  //       .then((data) => {
  //         loadPage(currentPage);
  //       })
  //       .catch((error) => {
  //         console.error("Error:", error);
  //       });
  //     }
  //   }
  // });

  // 전체 게시글 답글 필터
  let typeLinkItems = document.querySelectorAll('.type-filter');

  typeLinkItems.forEach((link) => {
    link.addEventListener("click", function (event) {
      event.preventDefault();

      document.querySelectorAll(".type-filter").forEach(
          (link) => link.classList.remove("active"));
      this.classList.add("active");
      const currentStatus = this.dataset.status;
      if (currentStatus !== "") {
        requestBody.reportObjectType = this.dataset.status;
      } else {
        requestBody.reportObjectType = null;
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
      nextBtn.style.display = currentIndex < imageUrls.length - 1 ? "block" : "none";
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

    const replyBox = document.querySelector(".reply-box");
    const replyProfileImageElement = document.querySelector(".reply-profile-image");
    const replyUserIdElement = document.querySelector(".reply-user-id");
    const replyPostTimeElement = document.querySelector(".reply-post-time");
    const replyContentElement = document.querySelector(".reply-content");

    // 기본적으로 reply-box 숨김 처리
    replyBox.style.display = "none";


    // type에 따라 데이터 설정
    if (reportData.type.data === "POST") {
      // POST 데이터 설정
      profileImageElement.src = reportData.post.user.profileImage || "/images/icons/UI-user2.png";
      userIdElement.textContent = reportData.post.user.nickname;
      postContentElement.textContent = reportData.post.content;
      postTimeElement.textContent = reportData.post.timeDifference;
    } else if (reportData.type.data === "REPLY") {
      // REPLY 데이터 설정
      profileImageElement.src = reportData.replyPost.post.user.profileImage || "/images/icons/UI-user2.png";
      userIdElement.textContent = reportData.replyPost.post.user.nickname;
      postContentElement.textContent = reportData.replyPost.post.content;
      postTimeElement.textContent = reportData.replyPost.post.timeDifference;

      // reply-box 데이터 매칭
      replyProfileImageElement.src = reportData.replyPost.user.profileImage || "/images/icons/UI-user2.png";
      replyUserIdElement.textContent = reportData.replyPost.user.nickname;
      replyPostTimeElement.textContent = reportData.replyPost.timeDifference;
      replyContentElement.textContent = reportData.replyPost.content;

      // reply-box 표시
      replyBox.style.display = "block";
    }

    const imageUrls = imageData.map(image => image.fileName);

    // 이미지 슬라이더 초기화
    initializeSlider(reportData, imageUrls);

    modalContainer.style.display = "block";
    modalBackground.style.display = "block";

    closeModalButton.addEventListener("click", function () {
      modalContainer.style.display = "none";
      modalBackground.style.display = "none";
      replyBox.style.display = "none"; // 모달 닫힐 때 reply-box 숨김 처리
    });

    modalBackground.addEventListener("click", function (event) {
      if (event.target === modalBackground) {
        modalContainer.style.display = "none";
        modalBackground.style.display = "none";
        replyBox.style.display = "none"; // 모달 닫힐 때 reply-box 숨김 처리
      }
    });
  }

  tableBody.addEventListener("click", function (event) {
    // 변경
    if (event.target.classList.contains("change-status")) {
      event.preventDefault();
      event.stopPropagation(); // 이벤트 전파 방지

      const postOrReplyId = event.target.dataset.id;
      const currentRow = event.target.closest('tr');
      const blindStatusCell = currentRow.querySelector('.blind-status');
      const postOrReply = event.target.dataset.value;
      let url = "";
      if (postOrReply === "POST") {
        url = `/admin/api/blind/post/${postOrReplyId}`;
      } else {
        url = `/admin/api/blind/reply/${postOrReplyId}`;
      }
      fetch(url, {
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

        // 동일한 게시글 신고 항목 블라인드 상태 업데이트
        document.querySelectorAll("tr").forEach((row) => {
          const rowType = row.querySelector("[data-value]")?.dataset.value;
          const rowId = row.querySelector(".change-status")?.dataset.id;

          if (rowType === postOrReply && rowId === postOrReplyId) {
            const blindCell = row.querySelector(".blind-status");
            if (blindCell) {
              blindCell.textContent = data.isBlind ? "O" : "X";
            }
          }
        });
      })
      .catch((error) => {
        console.error("Error:", error);
      });
      return
    }

    // 신고 목록 삭제
    if (event.target.classList.contains("delete-report")) {
      event.preventDefault(); // 기본 동작 방지
      event.stopPropagation(); // 이벤트 전파 방지
      if (confirm("정말로 신고를 삭제하시겠습니까? (관련된 신고목록 전부 삭제)")) {
        const reportId = event.target.dataset.id;

        fetch(`/admin/api/report/${reportId}`, {
          method: "DELETE",
        })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to delete post");
          }
          return response.text();
        })
        .then((data) => {
          loadPage(currentPage);
        })
        .catch((error) => {
          console.error("Error:", error);
        });
      }
      return;
    }

    // 게시글 댓글 삭제
    if (event.target.classList.contains("delete-post")) {
      event.preventDefault(); // 기본 동작 방지
      event.stopPropagation(); // 이벤트 전파 방지

      const postOrReplyId = event.target.dataset.id;
      const postOrReply = event.target.dataset.value;

      if (confirm("정말로 이 게시글/댓글을 삭제하시겠습니까?")) {
        if (postOrReply === "POST") {
          fetch(`/admin/api/report/post/${postOrReplyId}`, {
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
              if (confirm("해당 게시글/댓글은 신고된 게시글입니다. 정말 삭제하시겠습니까? (신고 목록도 삭제)")) {
                deletePost(postOrReplyId);
              }
            } else {
              deletePost(postOrReplyId);
            }
          })
          .catch((error) => {
            console.error("Error:", error);
            alert("게시글 삭제 중 오류가 발생했습니다.");
          });
        } else {
          fetch(`/admin/api/report/reply/${postOrReplyId}`, {
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
              if (confirm("해당 게시글/댓글은 신고된 게시글입니다. 정말 삭제하시겠습니까? (신고 목록도 삭제)")) {
                deleteReply(postOrReplyId);
              }
            } else {
              deleteReply(postOrReplyId);
            }
          })
          .catch((error) => {
            console.error("Error:", error);
            alert("게시글 삭제 중 오류가 발생했습니다.");
          });
        }

      }
      return;
    }


    const row = event.target.closest("tr");
    if (row) {
      const reportData = JSON.parse(row.dataset.report);  // 이미 바인딩된 데이터

      // type에 따라 postId 설정
      let postId;
      if (reportData.type.data === "POST") {
        postId = reportData.post.postId; // type이 POST일 경우
      } else if (reportData.type.data === "REPLY") {
        postId = reportData.replyPost.post.postId; // type이 REPLY일 경우
      }

      fetch(`/admin/api/post/${postId}/image`)
      .then(response => response.json())
      .then(imageData => {
        openProfileModal(reportData, imageData);
      })
      .catch(error => {
        console.error("이미지 로드 오류:", error);
      });
    }
  });
})
;
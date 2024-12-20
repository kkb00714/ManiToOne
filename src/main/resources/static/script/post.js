// 게시글 좋아요
function likePost(postId) {
  const likeCount = document.querySelector("#post-like-count");

  if (postId) {
    fetch(`/api/post/like/${postId}`, {
      method: "POST",
    })
      .then((response) => {
        if (response.status === 200) {
          if (likeCount) {
            fetch("/api/post/like/number/" + postId)
              .then((response) => response.text())
              .then((countText) => {
                const currentLikes = parseInt(countText, 10);
                likeCount.textContent = currentLikes;
              })
              .catch((error) => {
                console.error("Error fetching like count:", error);
              });
          }
        }
      })
      .catch((error) => {
        console.error("Error fetching like count:", error);
      });
  }
}

// 답글 좋아요
function likeReply(replyId) {
  const likeCount = document.querySelector("#reply-like-count");

  if (replyId) {
    fetch(`/api/reply/like/${replyId}`, {
      method: "POST",
    })
      .then((response) => {
        if (response.status === 200) {
          if (likeCount) {
            fetch("/api/reply/like/number/" + replyId)
              .then((response) => response.text())
              .then((countText) => {
                const currentLikes = parseInt(countText, 10);
                likeCount.textContent = currentLikes;
              })
              .catch((error) => {
                console.error("Error fetching like count:", error);
              });
          }
        }
      })
      .catch((error) => {
        console.error("Error fetching like count:", error);
      });
  }
}

// 답글 상세 페이지 이동
function directToReply(replyId) {
  window.location.href = `/reply/${replyId}`;
}

// 게시글 상세 페이지 이동
function directToPost(postId) {
  window.location.href = `/post/${postId}`;
}

// 마니또 여부 확인
let isManito = false;
let isFeedbackReq = false;

function togglesManito(element, responseType) {
  const imgElement = element.querySelector("img");

  if (!imgElement) {
    return;
  }

  const isChecked = imgElement.src.includes("icon-check.png");

  if (isChecked) {
    imgElement.src = imgElement
      .getAttribute("data-unchecked-src")
      .replace("@{", "")
      .replace("}", "");
    element.style.opacity = "0.3";
    isManito = false;
  } else {
    imgElement.src = imgElement
      .getAttribute("data-checked-src")
      .replace("@{", "")
      .replace("}", "");
    element.style.opacity = "1";
    isManito = true;
  }
}

async function togglesAIFeedback(element, responseType) {
  const imgElement = element.querySelector("img");
  const currentSrc = imgElement.getAttribute("src");

  if (currentSrc.includes("icon-check.png")) {
    imgElement.src = imgElement
      .getAttribute("data-unchecked-src")
      .replace("@{", "")
      .replace("}", "");
    element.style.opacity = "0.3";
    isFeedbackReq = false;
  } else {
    // 여기서 서버통신으로 AI 피드백을 오늘 하루 3개 이상 받았는지 확인
    // 서버 통신으로 AI 피드백 제한 확인
    try {
      const response = await fetch("/api/post/ai-feedback/count", { method: "GET" });
      const isLimitExceeded = await response.json();

      if (isLimitExceeded === null || isLimitExceeded === undefined) {
        alert("AI 피드백 시스템을 현재 이용할 수 없습니다.");
        return;
      }

      if (isLimitExceeded === true) {
        // 제한 초과일 경우 알림 표시 및 토글 중단
        alert("오늘은 더 이상 AI 피드백을 요청할 수 없습니다.");
        return;
      }

      // 제한 초과가 아닌 경우, 체크 로직 실행
      imgElement.src = imgElement
      .getAttribute("data-checked-src")
      .replace("@{", "")
      .replace("}", "");
      element.style.opacity = "1";
      isFeedbackReq = true;
    } catch (error) {
      console.error("서버 통신 오류:", error);
      alert("AI 피드백 상태를 확인할 수 없습니다. 다시 시도해주세요.");
    }
  }
}

// 이미지 개수 확인
function countImages(input) {
  const maxImages = 4;
  const imageCount = input.files.length;

  if (imageCount > maxImages) {
    alert(`사진은 최대 ${maxImages}장까지만 업로드 가능합니다.`);
    return;
  }
}

// 게시글 작성
async function onPostSubmit() {
  const baseUrl = "/api/post";

  const content = document.getElementById("new-post-content").value.trim();
  const images = document.getElementById("image-upload-btn").files;

  if (!content) {
    alert("내용을 입력해주세요.");
    return;
  }

  if (images.length > 4) {
    alert("이미지는 최대 4장까지만 업로드 가능합니다.");
    return;
  }

  let url = `${baseUrl}?content=${encodeURIComponent(content)}&isManito=${
    isManito ? "true" : "false"
  }&isFeedbackReq=${isFeedbackReq ? "true" : "false"}`;

  const formData = new FormData();
  for (let i = 0; i < images.length; i++) {
    formData.append("images", images[i]);
  }

  try {
    const response = await fetch(url, {
      method: "POST",
      body: formData,
    });

    if (response.ok) {
      alert("게시글을 작성하셨습니다.");
      window.location.reload();
    } else {
      alert("게시글 작성에 실패했습니다.");
    }
  } catch (error) {
    alert("게시글 작성 중 오류가 발생했습니다.");
  }
}

// AI 피드백 받기
async function getFeedback() {
  if (aiFeedback === false) {
    return;
  }

  const content = document.getElementById("new-post-content").value.trim();
  const container = document.getElementById("ai-feedback-container");
  const displayText = document.getElementById("ai-feedback-content");

  if (!content) {
    alert("내용을 입력해주세요.");
    return;
  }


  const url = `/api/post/ai-feedback?content=${encodeURIComponent(content)}`;

  try {
    const response = await fetch(url, {
      method: "GET",
    });

    if (!response.ok) {
      alert("AI 피드백 받기에 실패했습니다.");
      return;
    }

    const data = await response.json();

    container.style.display = "flex";
    displayText.value = `AI: ${data.feedback}`;
  } catch (error) {
    alert("AI 피드백 받는 도중에 오류가 발생했습니다.");
  }
}

// 게시글 추가 기능 모달
function openPostOptionsModal() {
  document.getElementById("post-options-modal").style.display = "flex";
}

function closePostOptionsModal(event) {
  if (event.target.id === "post-options-modal") {
    document.getElementById("post-options-modal").style.display = "none";
  }
}

document
  .getElementById("post-options-modal")
  .addEventListener("click", closePostOptionsModal);

// 답글 추가 기능 모달
function openReplyOptionsModal() {
  document.getElementById("reply-options-modal").style.display = "flex";
}

function closeReplyOptionsModal(event) {
  if (event.target.id === "reply-options-modal") {
    document.getElementById("reply-options-modal").style.display = "none";
  }
}

document
  .getElementById("reply-options-modal")
  .addEventListener("click", closeReplyOptionsModal);

// 답글의 답글 추가 기능 모달
function openRereplyOptionsModal() {
  document.getElementById("rereply-options-modal").style.display = "flex";
}

function closeRereplyOptionsModal(event) {
  if (event.target.id === "rereply-options-modal") {
    document.getElementById("rereply-options-modal").style.display = "none";
  }
}

document
  .getElementById("rereply-options-modal")
  .addEventListener("click", closeRereplyOptionsModal);

// 게시글 숨기기
function hidePost(postId) {
  fetch(`/api/post/hidden/${postId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
  }).then((response) => {
    if (response.ok) {
      alert("해당 게시글을 숨김 처리하셨습니다.");
      window.history.back();
    } else {
      alert("해당 게시글 숨김 처리에 실패했습니다.");
    }
  });
}

// 답글 숨기기
function hideReply(replyId) {
  fetch(`/api/reply/hidden/${replyId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
  }).then((response) => {
    if (response.ok) {
      alert("해당 답글을 숨김 처리하셨습니다.");
      window.location.reload();
    } else {
      alert("해당 답글 숨김 처리에 실패했습니다.");
    }
  });
}

// 게시글 삭제
function deletePost(postId) {
  fetch(`/api/post/${postId}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
  }).then((response) => {
    if (response.ok) {
      alert("해당 게시글을 삭제하셨습니다.");
      window.history.back();
    } else {
      alert("해당 게시글을 삭제하지 못했습니다.");
    }
  });
}

// 답글 삭제
function deleteReply(replyId) {
  fetch(`/api/reply/${replyId}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
  }).then((response) => {
    if (response.ok) {
      alert("해당 답글을 삭제하셨습니다.");
      window.location.reload();
    } else {
      alert("해당 답글을 삭제하지 못했습니다.");
    }
  });
}

// 게시글 신고 모달
function openPostReportModal() {
  document.getElementById("post-options-modal").style.display = "none";
  document.getElementById("postReportModal").style.display = "block";
  document.getElementById("postReportModalContainer").style.display = "block";
}

function closePostReportModal() {
  document.getElementById("postReportModal").style.display = "none";
  document.getElementById("postReportModalContainer").style.display = "none";
}

// 답글 신고 모달
function openReplyReportModal() {
  document.getElementById("reply-options-modal").style.display = "none";
  document.getElementById("replyReportModal").style.display = "block";
  document.getElementById("replyReportModalContainer").style.display = "block";
}

function closeReplyReportModal() {
  document.getElementById("replyReportModal").style.display = "none";
  document.getElementById("replyReportModalContainer").style.display = "none";
}

// 답글의 답글 신고 모달
function openRereplyReportModal() {
  document.getElementById("rereply-options-modal").style.display = "none";
  document.getElementById("reReplyReportModal").style.display = "block";
  document.getElementById("reReplyReportModalContainer").style.display =
    "block";
}

function closeRereplyReportModal() {
  document.getElementById("reReplyReportModal").style.display = "none";
  document.getElementById("reReplyReportModalContainer").style.display = "none";
}

// 게시글 신고 form
function onPostReportSubmit(event) {
  event.preventDefault();

  const form = event.target;
  const baseUrl = form.action;
  const reportType = document
    .getElementById("post-report-type-select")
    .value.trim();

  const url = `${baseUrl}?reportType=${encodeURIComponent(reportType)}`;

  fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
  }).then((response) => {
    if (response.ok) {
      alert("해당 게시글을 신고하셨습니다.");
      closePostReportModal();
    } else {
      alert("해당 게시글 신고에 실패했습니다.");
    }
  });
}

// 답글 신고 form
function onReplyReportSubmit(event) {
  event.preventDefault();

  const form = event.target;
  const baseUrl = form.action;
  const reportType = document
    .getElementById("reply-report-type-select")
    .value.trim();

  const url = `${baseUrl}?reportType=${encodeURIComponent(reportType)}`;

  fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
  }).then((response) => {
    if (response.ok) {
      alert("해당 게시글을 신고하셨습니다.");
      closeReplyReportModal();
    } else {
      alert("해당 게시글 신고에 실패했습니다.");
    }
  });
}

// 답글의 답글 신고 form
function onRereplyReportSubmit(event) {
  event.preventDefault();

  const form = event.target;
  const baseUrl = form.action;
  const reportType = document
    .getElementById("rereply-report-type-select")
    .value.trim();

  const url = `${baseUrl}?reportType=${encodeURIComponent(reportType)}`;

  fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
  }).then((response) => {
    if (response.ok) {
      alert("해당 게시글을 신고하셨습니다.");
      closeRereplyReportModal();
    } else {
      alert("해당 게시글 신고에 실패했습니다.");
    }
  });
}

// 게시글 수정 모달
function openUpdatePostModal() {
  document.getElementById("post-options-modal").style.display = "none";
  document.getElementById("updatePostFormModal").style.display = "block";
  document.getElementById("updatePostFormModalContainer").style.display =
    "block";
}

function closeUpdatePostModal() {
  document.getElementById("updatePostFormModal").style.display = "none";
  document.getElementById("updatePostFormModalContainer").style.display =
    "none";
}

// 답글 수정 모달
function openUpdateReplyModal() {
  document.getElementById("reply-options-modal").style.display = "none";
  document.getElementById("updateReplyFormModal").style.display = "block";
  document.getElementById("updateReplyFormModalContainer").style.display =
    "block";
}

function closeUpdateReplyModal() {
  document.getElementById("updateReplyFormModal").style.display = "none";
  document.getElementById("updateReplyFormModalContainer").style.display =
    "none";
}

// 답글의 답글 수정 모달
function openUpdateRereplyModal() {
  document.getElementById("rereply-options-modal").style.display = "none";
  document.getElementById("updateRereplyFormModal").style.display = "block";
  document.getElementById("updateRereplyFormModalContainer").style.display =
    "block";
}

function closeUpdateRereplyModal() {
  document.getElementById("updateRereplyFormModal").style.display = "none";
  document.getElementById("updateRereplyFormModalContainer").style.display =
    "none";
}

// 게시글 수정
async function onUpdatePostSubmit(postId, uploadedImagesNum) {
  const content = document.getElementById("new-post-content").value.trim();
  const images = document.getElementById("image-upload-btn").files;

  if (!content) {
    alert("내용을 입력해주세요.");
    return;
  }

  if (images.length > 4 || uploadedImagesNum + images.length > 4) {
    alert("이미지는 최대 4장까지만 업로드 가능합니다.");
    return;
  }

  console.log("images: ", images);

  const url = `/api/post/${postId}?content=${encodeURIComponent(content)}`;

  console.log("URL: ", url);

  const formData = new FormData();

  if (images.length > 0) {
    for (let i = 0; i < images.length; i++) {
      formData.append("images", images[i]);
    }
  }

  try {
    const response = await fetch(url, {
      method: "PUT",
      body: formData,
    });

    if (response.ok) {
      alert("게시글을 수정하셨습니다.");
      window.location.reload();
    } else {
      alert("게시글 수정에 실패했습니다.");
    }
  } catch (error) {
    alert("게시글 수정 중 오류가 발생했습니다.");
  }
}

// 답글 수정
async function onUpdateReplySubmit(replyId) {
  const content = document.getElementById("update-reply-content").value.trim();

  if (!content) {
    alert("내용을 입력해주세요.");
    return;
  }

  const url = `/api/reply/${replyId}?content=${encodeURIComponent(content)}`;

  try {
    const response = await fetch(url, {
      method: "PUT",
    });

    if (response.ok) {
      alert("답글을 수정하셧습니다.");
      window.location.reload();
    } else {
      alert("답글 수정에 실패했습니다.");
    }
  } catch (error) {
    alert("답글 수정 중 오류가 발생했습니다.");
  }
}

// 답글의 답글 수정
async function onUpdateRereplySubmit(replyId) {
  const content = document
    .getElementById("update-rereply-content")
    .value.trim();

  if (!content) {
    alert("내용을 입력해주세요.");
    return;
  }

  const url = `/api/reply/${replyId}?content=${encodeURIComponent(content)}`;

  try {
    const response = await fetch(url, {
      method: "PUT",
    });

    if (response.ok) {
      alert("답글을 수정하셧습니다.");
      window.location.reload();
    } else {
      alert("답글 수정에 실패했습니다.");
    }
  } catch (error) {
    alert("답글 수정 중 오류가 발생했습니다.");
  }
}

// 답글 작성 모달
function openNewReplyModal() {
  document.getElementById("newReplyFormModal").style.display = "block";
  document.getElementById("newReplyFormModalContainer").style.display = "block";
}

function closeNewReplyModal(event) {
  if (event && event.target && event.target.closest('#newReplyFormModalContainer')) {
    return;
  }
  document.getElementById("newReplyFormModal").style.display = "none";
  document.getElementById("newReplyFormModalContainer").style.display = "none";
}

const initializeReplyModalEvents = () => {
  const modal = document.getElementById('newReplyFormModal');
  if (modal) {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        closeNewReplyModal(e);
      }
    });
  }

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && document.getElementById('newReplyFormModal')?.style.display === 'block') {
      closeNewReplyModal();
    }
  });
};

// 답글 작성
async function onNewReplySubmit(postId) {
  const content = document.getElementById("new-reply-content").value.trim();

  if (!content) {
    alert("내용을 입력해주세요.");
    return;
  }

  const url = `/api/reply/${postId}?content=${encodeURIComponent(content)}`;

  try {
    const response = await fetch(url, {
      method: "POST",
    });

    if (response.ok) {
      alert("답글을 작성하셨습니다.");
      window.location.reload();
    } else {
      alert("답글 작성에 실패했습니다.");
    }
  } catch (error) {
    alert("답글 작성 중 오류가 발생했습니다.");
  }
}

// 답글의 답글 작성 모달
function openNewRereplyModal() {
  document.getElementById("newRereplyFormModal").style.display = "block";
  document.getElementById("newRereplyFormModalContainer").style.display =
    "block";
}

function closeNewRereplyModal() {
  document.getElementById("newRereplyFormModal").style.display = "none";
  document.getElementById("newRereplyFormModalContainer").style.display =
    "none";
}

// 답글의 답글 작성
async function onNewRereplySubmit(replyId) {
  const content = document.getElementById("new-rereply-content").value.trim();

  if (!content) {
    alert("내용을 입력해주세요.");
    return;
  }

  const url = `/api/rereply/${replyId}?content=${encodeURIComponent(content)}`;

  try {
    const response = await fetch(url, {
      method: "POST",
    });

    if (response.ok) {
      alert("답글을 작성하셨습니다.");
      window.location.reload();
    } else {
      alert("답글 작성에 실패했습니다.");
    }
  } catch (error) {
    alert("답글 작성 중 오류가 발생했습니다.");
  }
}

// 프로필 조회
function directToProfilePost() {
  const nickname = document.querySelector(".post-user-name").textContent;
  console.log("nickname: ", nickname);
  window.location.href = `/profile/${nickname}`;
}

function directToProfileReply() {
  const nickname = document.querySelector(".reply-user-name").textContent;
  console.log("nickname: ", nickname);
  window.location.href = `/profile/${nickname}`;
}

function directToProfileRereply() {
  const nickname = document.querySelector(".rereply-user-name").textContent;
  console.log("nickname: ", nickname);
  window.location.href = `/profile/${nickname}`;
}

// 유저 팔로우
function toggleFollowPost() {
  const nickname = document.querySelector(".post-user-name").textContent;

  console.log("nickname: ", nickname);

  if (nickname) {
    fetch(`/api/follow/${nickname}`, {
      method: "GET",
    })
      .then((response) => {
        if (response.status === 201 || response.status === 200) {
          alert(`${nickname}님을 팔로우하셨습니다.`);
          window.location.reload();
        } else {
          alert(`${nickname}님 팔로우에 실패했습니다.`);
        }
      })
      .catch((error) => {
        alert(`${nickname}님 팔로우에 실패했습니다.`);
      });
  }
}

function toggleFollowReply() {
  const nickname = document.querySelector(".reply-user-name").textContent;

  console.log("nickname: ", nickname);

  if (nickname) {
    fetch(`/api/follow/${nickname}`, {
      method: "GET",
    })
      .then((response) => {
        if (response.status === 201 || response.status === 200) {
          alert(`${nickname}님을 팔로우하셨습니다.`);
          window.location.reload();
        } else {
          alert(`${nickname}님 팔로우에 실패했습니다.`);
        }
      })
      .catch((error) => {
        alert(`${nickname}님 팔로우에 실패했습니다.`);
      });
  }
}

function toggleFollowRereply() {
  const nickname = document.querySelector(".rereply-user-name").textContent;

  console.log("nickname: ", nickname);

  if (nickname) {
    fetch(`/api/follow/${nickname}`, {
      method: "GET",
    })
      .then((response) => {
        if (response.status === 201 || response.status === 200) {
          alert(`${nickname}님을 팔로우하셨습니다.`);
          window.location.reload();
        } else {
          alert(`${nickname}님 팔로우에 실패했습니다.`);
        }
      })
      .catch((error) => {
        alert(`${nickname}님 팔로우에 실패했습니다.`);
      });
  }
}

document.addEventListener('DOMContentLoaded', () => {
  initializeReplyModalEvents();
});

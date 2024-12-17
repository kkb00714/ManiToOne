// 게시글 좋아요
async function likePost(postId) {
  try {
    const response = await fetch(`/api/post/like/${postId}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (response.ok) {
      alert("해당 게시글에 좋아요를 누르셨습니다.");
    } else {
      alert("좋아요 요청에 실패하셨습니다.");
    }
  } catch (error) {
    console.error("좋아요 요청 오류: ", error);
    alert("좋아요 요청 중 오류가 발생했습니다.");
  }
}

// 답글 좋아요
async function likeReply(replyId) {
  try {
    const response = await fetch(`/api/reply/like/${replyId}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (response.ok) {
      alert("해당 댓글에 좋아요를 누르셨습니다.");
    } else {
      alert("좋아요 요청에 실패하셨습니다.");
    }
  } catch (error) {
    console.error("좋아요 요청 오류: ", error);
    alert("좋아요 요청 중 오류가 발생했습니다.");
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

  console.log(`isManito: ${isManito}`);
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
async function onPostSubmit(event) {
  event.preventDefault();

  const form = event.target;
  const baseUrl = form.action;

  const content = document.getElementById("new-post-content").value.trim();
  const images = document.getElementById("image-upload-btn").files;

  console.log("content: ", content);
  console.log("images: ", images);

  if (!content) {
    alert("내용을 입력해주세요.");
    return;
  }

  if (images.length > 4) {
    alert("이미지는 최대 4장까지만 업로드 가능합니다.");
    return;
  }

  const formData = new URLSearchParams();
  formData.append("content", content);
  formData.append("isManito", isManito);
  // formData.append("images", images);

  for (let i = 0; i < images.length; i++) {
    formData.append("images", images[i]);
  }

  console.log("Data: ", formData);
  
  try {
    const response = await fetch(baseUrl, {
      method: "POST",
      body: formData,
    });

    if (response.ok) {
      alert("게시글을 작성하셨습니다.");
      window.location.reload();
    } else {
      alert("게시글 작성에 실패하셨습니다.");
    }
  } catch (error) {
    alert("오류가 발생했습니다.");
    console.log("게시글 작성 오류: ", error);
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
      window.location.href = "/";
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
      window.location.href = "/";
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

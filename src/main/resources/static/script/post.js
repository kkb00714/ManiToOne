// 게시글 좋아요
async function likePost(postId) {
  try {
    const response = await fetch(`/api/post/like/${postId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (response.ok) {
      alert("해당 게시글에 좋아요를 누르셨습니다.");
    } else {
      alert("좋아요 요청에 실패하셨습니다.")
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
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (response.ok) {
      alert("해당 댓글에 좋아요를 누르셨습니다.");
    } else {
      alert("좋아요 요청에 실패하셨습니다.")
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

async function thisIsManito() {
  isManito = true;
}

console.log(isManito);

// 게시글 작성
async function submitPost() {
  const content = document.getElementById('new-post-content').value.trim();
  console.log(content);

  const postData = {
    content: content,
    isManito: isManito
  };
  console.log(postData);

  try {
    const response = await fetch('/api/post', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(postData)
    });

    if (response.ok) {
      alert("게시글 작성을 완료하셨습니다.");
      location.reload();
    } else {
      alert("게시글 작성에 실패하셨습니다.");
    }
  } catch (error) {
    console.error("게시글 작성 오류: ", error);
    alert("게시글 작성 중 오류가 발생했습니다.");
  }
}

// 게시글 추가 기능 모달
function openPostOptionsModal() {
  document.getElementById('post-options-modal').style.display = 'flex';
}

function closePostOptionsModal(event) {
  if (event.target.id === 'post-options-modal') {
    document.getElementById('post-options-modal').style.display = 'none';
  }
}

document.getElementById('post-options-modal').addEventListener('click', closePostOptionsModal);

// 답글 추가 기능 모달
function openReplyOptionsModal() {
  document.getElementById('reply-options-modal').style.display = 'flex';
}

function closeReplyOptionsModal(event) {
  if (event.target.id === 'reply-options-modal') {
    document.getElementById('reply-options-modal').style.display = 'none';
  }
}

document.getElementById('reply-options-modal').addEventListener('click', closeReplyOptionsModal);

// 답글의 답글 추가 기능 모달
function openRereplyOptionsModal() {
  document.getElementById('rereply-options-modal').style.display = 'flex';
}

function closeRereplyOptionsModal(event) {
  if (event.target.id === 'rereply-options-modal') {
    document.getElementById('rereply-options-modal').style.display = 'none';
  }
}

document.getElementById('rereply-options-modal').addEventListener('click', closeRereplyOptionsModal);

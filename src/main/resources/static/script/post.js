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

// 게시글 작성
async function submitPost() {
  const content = document.getElementById('new-post-content').value.trim();

  const postData = {
    content: this.content,
    isManito: this.isManito
  };

  try {
    const response = await fetch('/api/post', {
      method: 'POST',
      headers: {
        'Content-Type': 'applicaton/json'
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
    console.error("게시글 작성 에러: ", error);
    alert("게시글 작성 중 에러가 발생했습니다.");
  }
}
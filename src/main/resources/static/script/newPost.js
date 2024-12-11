// 새로운 게시글 생성
const inputText = document.getElementById("new-post-content").value;

let isManito = 0;
const maxImages = 4;

// 게시하기 버튼 클릭 이벤트 추가
document.querySelector('.post-button').addEventListener('click', function() {
  const content = document.getElementById("new-post-content").value.trim();

  if (!content) {
    const modal = new BaseModal(
        "newPostFormModalContainer",
        "newPostFormModalBackground",
        "openPostFormModalBtn",
        "closePostFormModalBtn"
    );
    modal.showWarning('내용을 작성해주세요');
    return;
  }

  // 여기에 게시글 생성 로직 추가
  // 예: createPost(content);
});
document.addEventListener("DOMContentLoaded", function () {
  let pageNum = 0;
  const postsContainer = document.querySelector(".timeline-posts");
  let isLoading = false;
  let hasMorePosts = true;

  loadPosts();

  window.addEventListener('scroll', handleScroll);

  function handleScroll() {
    if (window.innerHeight + window.scrollY >= document.body.scrollHeight - 100) {
      if (!isLoading && hasMorePosts) {
        pageNum++;
        loadPosts();
      }
    }
  }

  function loadPosts() {
    if (isLoading) return;

    isLoading = true;
    showLoader();

    fetch(`/api/timeline?page=${pageNum}&size=20`)
    .then(response => response.json())
    .then(data => {
      if (data.content && data.content.length > 0) {
        data.content.forEach(post => {
          const postElement = createPostElement(post);
          postsContainer.appendChild(postElement);
        });
        hasMorePosts = !data.last;
      } else {
        hasMorePosts = false;
        if (pageNum === 0) {
          showEmptyState();
        }
      }
    })
    .catch(error => {
      console.error('Error loading posts:', error);
      showError();
    })
    .finally(() => {
      isLoading = false;
      hideLoader();
    });
  }

  function createPostElement(post) {
    const postElement = document.createElement("div");
    postElement.classList.add("post-container");

    const timeText = post.updatedAt ?
        `(수정됨) ${timeForToday(post.updatedAt)}` :
        timeForToday(post.createdAt);

    postElement.innerHTML = `
      <img 
        class="user-photo" 
        src="${post.profileImage || '/images/icons/UI-user2.png'}" 
        alt="user icon"
      />
      <div class="post-content">
        <div class="user-info">
          <a href="/profile/${post.nickName}" class="user-name">${post.nickName}</a>
          <span class="passed-time">${timeText}</span>
        </div>
        <p class="content-text">${post.content}</p>
        ${createImagesHTML(post.postImages)}
        <div class="reaction-icons">
          <img 
            class="tiny-icons" 
            src="/images/icons/icon-clover2.png" 
            alt="I like this"
          />
          <span class="like-count">${post.likeCount}</span>
          <img 
            class="tiny-icons" 
            src="/images/icons/icon-comment2.png" 
            alt="add reply"
          />
          <span class="reply-count">${post.replies.length}</span>
        </div>
      </div>
      <div class="option-icons">
        <img 
          class="tiny-icons" 
          src="/images/icons/UI-more2.png" 
          alt="more options"
        />
        <img 
          class="tiny-icons" 
          src="/images/icons/icon-add-friend.png" 
          alt="add friend" 
          data-target-id="${post.nickName}"
        />
      </div>
    `;

    return postElement;
  }

  function createImagesHTML(images) {
    if (!images || images.length === 0) return '';

    return `
      <img 
        class="post-image" 
        src="/images/upload/${images[0].fileName}" 
        alt="post image"
      />
    `;
  }

  function timeForToday(value) {
    const today = new Date();
    const timeValue = new Date(value);
    const betweenTime = Math.floor(
        (today.getTime() - timeValue.getTime()) / 1000 / 60
    );

    if (betweenTime < 1) return '방금전';
    if (betweenTime < 60) return `${betweenTime}분전`;

    const betweenTimeHour = Math.floor(betweenTime / 60);
    if (betweenTimeHour < 24) return `${betweenTimeHour}시간전`;

    const betweenTimeDay = Math.floor(betweenTime / 60 / 24);
    if (betweenTimeDay < 30) return `${betweenTimeDay}일전`;
    if (betweenTimeDay < 365) return `${Math.floor(betweenTimeDay / 30)}개월전`;

    return `${Math.floor(betweenTimeDay / 365)}년전`;
  }

  function showLoader() {
    const loader = document.createElement('div');
    loader.className = 'timeline-loader';
    loader.innerHTML = '<div class="loader-content">로딩 중...</div>';
    postsContainer.appendChild(loader);
  }

  function hideLoader() {
    const loader = document.querySelector('.timeline-loader');
    if (loader) loader.remove();
  }

  function showEmptyState() {
    postsContainer.innerHTML = `
      <div class="empty-timeline">
        <div class="empty-timeline-content">
          <img 
            src="/images/icons/UI-clover2.png" 
            alt="empty state icon" 
            class="empty-timeline-icon"
          />
          <h2 class="empty-timeline-title">아직 표시할 게시물이 없습니다</h2>
          <p class="empty-timeline-description">
            다른 사용자를 팔로우하고 새로운 이야기를 발견해보세요!
          </p>
        </div>
      </div>
    `;
  }

  function showError() {
    if (pageNum === 0) {
      postsContainer.innerHTML = `
        <div class="timeline-error">
          <p>게시물을 불러오는 중 오류가 발생했습니다.</p>
          <button onclick="location.reload()">다시 시도</button>
        </div>
      `;
    }
  }
});
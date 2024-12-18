document.addEventListener("DOMContentLoaded", function () {
  let pageNum = 0;
  const postsContainer = document.querySelector(".timeline-posts");
  let isLoading = false;
  let hasMorePosts = true;
  const nickname = document.querySelector('meta[name="user-nickname"]').content;

  loadPosts();
  window.addEventListener('scroll', handleScroll);
  postsContainer.addEventListener('click', function(event) {
    const moreOptionsButton = event.target.closest('.tiny-icons[alt="more options"]');
    if (moreOptionsButton) {
      handleMoreOptionsClick(event, moreOptionsButton);
    }
  });

  function handleScroll() {
    if (window.innerHeight + window.scrollY >= document.body.scrollHeight - 100) {
      if (!isLoading && hasMorePosts) {
        pageNum++;
        loadPosts();
      }
    }
  }

  async function loadPosts() {
    if (isLoading) return;

    isLoading = true;
    if (pageNum === 0) showLoader();

    try {
      const response = await fetch(`/api/timeline?page=${pageNum}&size=20`);
      const data = await response.json();

      if (data.content && data.content.length > 0) {
        let hasAddedPosts = false;

        for (const post of data.content) {
          const postElement = await createPostElement(post);
          postsContainer.appendChild(postElement);
          hasAddedPosts = true;

          // 첫 번째 포스트가 추가되면 로딩 인디케이터 제거
          if (hasAddedPosts && pageNum === 0) {
            hideLoader();
          }
        }

        hasMorePosts = !data.last;
        attachEventListeners();
      } else {
        hasMorePosts = false;
        if (pageNum === 0) {
          showEmptyState();
        }
      }
    } catch (error) {
      console.error('Error loading posts:', error);
      showError();
    } finally {
      isLoading = false;
      hideLoader();
    }
  }


  function attachEventListeners() {
    // 기존의 이벤트 리스너들을 제거
    removeExistingEventListeners();

    addFriendButtonsEventListener();
    addHidePostEventListener();
    addReportPostEventListener();
    addPostLikeEventListener();
    addPostDeleteEventHandler();
    postContentEventListener();
  }

  function removeExistingEventListeners() {
    const elements = {
      'img[alt="add friend"]': 'click',
      '.hide-post': 'click',
      '.report-post': 'click',
      'img[alt="I like this"]': 'click',
      '.delete-post': 'click',
      '.content-text': 'click'
    };

    for (const [selector, event] of Object.entries(elements)) {
      const elements = document.querySelectorAll(selector);
      elements.forEach(element => {
        element.replaceWith(element.cloneNode(true));
      });
    }
  }

  async function createPostElement(post) {
    const isFollowed = await getIsFollowed(post.nickName);
    const isMyPost = post.nickName === nickname;

    const postElement = document.createElement("div");
    postElement.classList.add("post-container");

    const timeText = post.updatedAt ?
        `(수정됨) ${timeForToday(post.updatedAt)}` :
        timeForToday(post.createdAt);

    postElement.innerHTML = `
      <a href="/profile/${post.nickName}">
        <img class="user-photo" src="${post.profileImage || '/images/icons/UI-user2.png'}" alt="user icon" />
      </a>
      <div class="post-content">
        <div class="user-info">
          <a href="/profile/${post.nickName}" class="user-name">${post.nickName}</a>
          <span class="passed-time" data-created-at="${post.createdAt}" data-updated-at="${post.updatedAt}">${timeText}</span>
        </div>
        <p class="content-text" data-post-id="${post.postId}">${post.content}</p>
        ${post.postImages ? createImagesHTML(post.postImages) : ''}
        <div class="reaction-icons">
          ${isMyPost
        ? `<img class="tiny-icons" src="/images/icons/icon-clover2.png" alt="my post"/>`
        : `<img class="tiny-icons" src="/images/icons/icon-clover2.png" alt="I like this" data-post-id="${post.postId}"/>`}
          <span class="like-count">${post.likeCount}</span>
          <img class="tiny-icons" src="/images/icons/icon-comment2.png" alt="add reply" />
          <span class="reply-count">${post.replies.length}</span>
        </div>
      </div>
      <div class="option-icons">
        <img class="tiny-icons" src="/images/icons/UI-more2.png" alt="more options" />
        ${(!isMyPost && !isFollowed)
        ? `<img class="tiny-icons" src="/images/icons/icon-add-friend.png" alt="add friend" data-target-id="${post.nickName}"/>`
        : ''} 
        <div class="more-options-menu hidden">
          <ul>
            ${isMyPost
        ? `
                <li><a href="#" class="hide-post" data-post-id="${post.postId}">숨기기</a></li>
                <hr>
                <li><a href="#" class="delete-post" data-post-id="${post.postId}">삭제하기</a></li>
              `
        : `<li><a href="#" class="report-post" data-post-id="${post.postId}">신고하기</a></li>`}
          </ul>
        </div>
      </div>
    `;

    return postElement;
  }

  function createImagesHTML(images) {
    if (!images || images.length === 0) return '';
    return `<img class="post-image" src="/images/upload/${images[0].fileName}" alt="post image"/>`;
  }

  async function getIsFollowed(nickName) {
    try {
      const response = await fetch(`/api/follow/followed/${nickName}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      const body = await response.text();
      return body === 'true';
    } catch (error) {
      console.error("팔로우 상태를 가져오는데 실패했습니다:", error);
      return false;
    }
  }

  function timeForToday(value) {
    const today = new Date();
    const timeValue = new Date(value);
    const betweenTime = Math.floor(
        (today.getTime() - timeValue.getTime()) / 1000 / 60);

    if (betweenTime < 1) return '방금전';
    if (betweenTime < 60) return `${betweenTime}분전`;

    const betweenTimeHour = Math.floor(betweenTime / 60);
    if (betweenTimeHour < 24) return `${betweenTimeHour}시간전`;

    const betweenTimeDay = Math.floor(betweenTime / 60 / 24);
    if (betweenTimeDay < 30) return `${betweenTimeDay}일전`;
    if (betweenTimeDay < 365) return `${Math.floor(betweenTimeDay / 30)}개월전`;

    return `${Math.floor(betweenTimeDay / 365)}년전`;
  }

  function addFriendButtonsEventListener() {
    const addFriendButtons = document.querySelectorAll('img[alt="add friend"]');
    addFriendButtons.forEach(button =>
        button.addEventListener("click", handleAddFriendClick));
  }

  function handleAddFriendClick() {
    const targetId = this.dataset.targetId;
    fetch(`/api/follow/${targetId}`)
    .then(response => {
      if (response.status === 201 || response.status === 200) {
        const allFollowButtonsForUser = document.querySelectorAll(
            `img[alt="add friend"][data-target-id="${targetId}"]`
        );
        allFollowButtonsForUser.forEach(button => {
          button.style.display = 'none';
        });

        const allPostsForUser = document.querySelectorAll(
            `.post-container .user-info a[href="/profile/${targetId}"]`
        );
        allPostsForUser.forEach(userLink => {
          const postContainer = userLink.closest('.post-container');
          if (postContainer) {
            const optionIcons = postContainer.querySelector('.option-icons');
            if (optionIcons) {
              const followButton = optionIcons.querySelector(`img[data-target-id="${targetId}"]`);
              if (followButton) {
                followButton.style.display = 'none';
              }
            }
          }
        });

        const feedbackToast = document.createElement('div');
        feedbackToast.className = 'feedback-toast';
        feedbackToast.textContent = '팔로우가 완료되었습니다.';
        document.body.appendChild(feedbackToast);

        feedbackToast.style.position = 'fixed';
        feedbackToast.style.bottom = '20px';
        feedbackToast.style.left = '50%';
        feedbackToast.style.transform = 'translateX(-50%)';
        feedbackToast.style.backgroundColor = 'rgba(0, 0, 0, 0.8)';
        feedbackToast.style.color = 'white';
        feedbackToast.style.padding = '10px 20px';
        feedbackToast.style.borderRadius = '4px';
        feedbackToast.style.zIndex = '1000';

        setTimeout(() => {
          feedbackToast.remove();
        }, 3000);
      } else {
        alert("오류가 발생했습니다.");
      }
    })
    .catch(error => console.error("API 호출 중 오류 발생:", error));
  }

  function postContentEventListener() {
    const postContents = document.querySelectorAll('.content-text');
    postContents.forEach(content =>
        content.addEventListener("click", handlePostContentClick));
  }

  function handlePostContentClick() {
    const postId = this.dataset.postId;
    location.href = "/post/" + postId;
  }

  function handleMoreOptionsClick(event, moreOptionsButton) {
    const optionsMenu = moreOptionsButton.closest('.option-icons').querySelector('.more-options-menu');
    if (!optionsMenu) return;

    const offsetX = event.clientX;
    const offsetY = event.clientY;
    const scrollY = window.scrollY;

    optionsMenu.style.top = `${offsetY + scrollY}px`;
    optionsMenu.style.left = `${offsetX}px`;

    const isMenuVisible = optionsMenu.classList.contains('visible');

    document.querySelectorAll('.more-options-menu').forEach(menu => {
      menu.classList.remove('visible');
      menu.classList.add('hidden');
    });

    if (isMenuVisible) {
      optionsMenu.classList.remove('visible');
      optionsMenu.classList.add('hidden');
    } else {
      optionsMenu.classList.remove('hidden');
      optionsMenu.classList.add('visible');
    }

    event.stopPropagation();
  }


  function showLoader() {
    const existingLoader = document.querySelector('.timeline-loader');
    if (!existingLoader) {
      const loader = document.createElement('div');
      loader.className = 'timeline-loader';
      loader.innerHTML = '<div class="loader-content">로딩 중...</div>';
      postsContainer.appendChild(loader);
    }
  }

  function hideLoader() {
    const loader = document.querySelector('.timeline-loader');
    if (loader) {
      loader.remove();
    }
  }

  function showEmptyState() {
    postsContainer.innerHTML = `
      <div class="empty-timeline">
        <div class="empty-timeline-content">
          <img src="/images/icons/UI-clover2.png" alt="empty state icon" class="empty-timeline-icon"/>
          <h2 class="empty-timeline-title">아직 표시할 게시물이 없습니다</h2>
          <p class="empty-timeline-description">다른 사용자를 팔로우하고 새로운 이야기를 발견해보세요!</p>
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

  function addHidePostEventListener() {
    const hidePostButtons = document.querySelectorAll('.hide-post');
    hidePostButtons.forEach(button => {
      button.addEventListener('click', function () {
        const postId = this.dataset.postId;

        if (postId) {
          fetch('/api/post/hidden/' + postId, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({postId: postId}),
          })
          .then(response => {
            if (response.ok) {
              alert('게시글 숨기기가 완료되었습니다.');
              // 숨기기 성공 시 해당 게시글 요소 제거
              const postContainer = button.closest('.post-container');
              postContainer.remove();
            } else {
              alert('게시글 숨기기에 실패했습니다.');
            }
          })
          .catch(error => {
            console.error('Error:', error);
            alert(error.message || '요청에 실패했습니다.');
          });
        } else {
          alert('게시글 ID를 찾을 수 없습니다.');
        }
      });
    });
  }

  function addPostLikeEventListener() {
    const likePostButtons = document.querySelectorAll('img[alt="I like this"]');
    likePostButtons.forEach(button => {
      button.addEventListener('click', async function () {
        const postId = this.dataset.postId;
        if (!postId) return;

        try {
          const likeResponse = await fetch('/api/post/like/' + postId, {
            method: 'POST'
          });

          if (likeResponse.ok) {
            const numberResponse = await fetch('/api/post/like/number/' + postId);
            const countText = await numberResponse.text();
            const currentLikes = parseInt(countText, 10);

            const likeCountElement = button.closest('div').querySelector('.like-count');
            if (likeCountElement) {
              likeCountElement.textContent = currentLikes;
              button.classList.toggle('liked');
            }
          }
        } catch (error) {
          console.error('Error handling like:', error);
        }
      });
    });
  }

  function addPostDeleteEventHandler() {
    const deletePostButtons = document.querySelectorAll('.delete-post');
    deletePostButtons.forEach(button => {
      button.addEventListener('click', function () {
        if (!confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
          return;
        }
        const postId = this.dataset.postId;
        if (postId) {
          fetch('/api/post/' + postId, {
            method: 'DELETE'
          })
          .then(response => {
            if (response.status === 200) {
              alert('게시글이 삭제되었습니다.');
              const postContainer = button.closest('.post-container');
              postContainer.remove();
            }
          });
        }
      });
    });
  }

  function addReportPostEventListener() {
    const reportPostButtons = document.querySelectorAll('.report-post');
    const reportModal = document.getElementById('reportModal');
    const reportModalContainer = document.getElementById('reportModalContainer');
    const closeReportModalButton = document.getElementById('closeReportModalBtn');
    const reportSendButton = document.getElementById('reportSendBtn');
    const reportTypeSelect = document.getElementById('report-type-select');

    let isReportButtonClicked = false;

    reportPostButtons.forEach(button => {
      button.addEventListener('click', function () {
        const postId = button.getAttribute('data-post-id');
        reportModalContainer.setAttribute('data-post-id', postId);
        reportModal.style.display = 'block';
        reportModalContainer.style.display = 'block';
        isReportButtonClicked = false;
      });
    });

    if (reportSendButton && !reportSendButton.hasEventListener) {
      reportSendButton.addEventListener('click', handleReportSubmit);
      reportSendButton.hasEventListener = true;
    }

    if (closeReportModalButton) {
      closeReportModalButton.addEventListener('click', function () {
        reportModal.style.display = 'none';
        reportModalContainer.style.display = 'none';
      });
    }

    window.addEventListener('click', function (event) {
      if (event.target === reportModal) {
        reportModal.style.display = 'none';
        reportModalContainer.style.display = 'none';
      }
    });

    function handleReportSubmit(event) {
      event.preventDefault();

      if (isReportButtonClicked) {
        alert('이미 신고가 제출되었습니다.');
        return;
      }

      const selectedReportType = reportTypeSelect.value;
      const postId = reportModalContainer.getAttribute('data-post-id');

      if (!selectedReportType) {
        alert('신고 사유를 선택해주세요.');
        return;
      }

      isReportButtonClicked = true;

      // URL에 reportType을 쿼리 파라미터로 추가
      fetch(`/api/post/report/${postId}?reportType=${selectedReportType}`, {
        method: 'POST',  // PUT에서 POST로 변경
        headers: {
          'Content-Type': 'application/json',
        }
      })
      .then(response => {
        if (response.ok) {
          alert('신고가 완료되었습니다.');
          reportModal.style.display = 'none';
          reportModalContainer.style.display = 'none';
        } else {
          return response.json().then(errorData => {
            alert(`신고 실패: ${errorData.message || '알 수 없는 오류 발생'}`);
          });
        }
      })
      .catch(error => {
        alert('네트워크 오류가 발생했습니다. 다시 시도해 주세요.');
      });
    }
  }
});
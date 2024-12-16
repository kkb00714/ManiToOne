document.addEventListener("DOMContentLoaded", function () {
  let pageNum = 0;
  const postsContainer = document.getElementById("postsContainer");
  const newPostForm = document.querySelector(".new-post-form");
  let isLoading = false;
  let hasMorePosts = true;
  let currentCategory = 1; // 1: 내 피드, 2: 좋아요 누른 피드, 3: 숨긴 피드

  window.addEventListener('scroll', handleScroll);

  document.querySelector('.my-post-menu-switch').addEventListener('click',
      () => {
        switchCategory(1);
        newPostForm.classList.remove('hidden');
      });
  document.querySelector('.like-it-menu-switch').addEventListener('click',
      () => {
        switchCategory(2);
        newPostForm.classList.add('hidden');
      });
  if (document.querySelector('.hidden-post-menu-switch')) {
    document.querySelector('.hidden-post-menu-switch').addEventListener('click',
        () => {
          switchCategory(3);
          newPostForm.classList.add('hidden');
        });
  }

  loadPosts(pageNum);

  function handleScroll() {
    if (window.innerHeight + window.scrollY >= document.body.scrollHeight
        - 100) {
      if (!isLoading && hasMorePosts) {
        isLoading = true;
        pageNum++;
        loadPosts(pageNum);
      }
    }
  }

  function switchCategory(category) {
    currentCategory = category;
    pageNum = 0;
    hasMorePosts = true;
    postsContainer.innerHTML = '';
    loadPosts(pageNum);
  }

  function loadPosts(pageNum) {
    const apiUrl = getApiUrl(pageNum);
    fetch(apiUrl)
    .then(response => response.json())
    .then(posts => {
      if (posts && posts.length > 0) {
        posts.forEach(post => {
          const postElement = createPostElement(post);
          postsContainer.appendChild(postElement);
        });
      } else {
        hasMorePosts = false;
      }
      isLoading = false;
      addFriendButtonsEventListener();
      addHidePostEventListener();
      addReportPostEventListener();
      addDocumentClickEventListener();
      addPostLikeEventListener();
      addPostDeleteEventHandler();
    })
    .catch(error => {
      console.error('Error loading posts:', error);
      isLoading = false;
    });
  }

  function getApiUrl(pageNum) {
    if (currentCategory === 1) {
      return `/api/post/by/${userNickName}?page=${pageNum}`;
    } else if (currentCategory === 2) {
      return `/api/post/${userNickName}/liked?page=${pageNum}`;
    } else if (currentCategory === 3) {
      return `/api/post/hidden?page=${pageNum}`;
    }
  }

  function createPostElement(post) {
    const postElement = document.createElement("div");
    postElement.classList.add("post-container");

    const timeText = post.updatedAt ? `(수정됨) ${timeForToday(post.updatedAt)}`
        : timeForToday(post.createdAt);

    postElement.innerHTML = `
          <img class="user-photo" src="${post.profileImage
    || '/images/icons/UI-user2.png'}" alt="user icon" />
          <div class="post-content">
            <div class="user-info">
              <a href="/profile/${post.nickName}" class="user-name">${post.nickName}</a>
              <span class="passed-time" data-created-at="${post.createdAt}" data-updated-at="${post.updatedAt}">${timeText}</span>
            </div>
            <p class="content-text">${post.content}</p>
            <div class="reaction-icons">
            ${myNickName !== post.nickName
        ? `<img class="tiny-icons" src="/images/icons/icon-clover2.png" alt="I like this" data-post-id="${post.postId}"/>`
        : `<img class="tiny-icons" src="/images/icons/icon-clover2.png" alt="my post"/>`}
              <span class="like-count">${post.likeCount}</span>
              <img class="tiny-icons" src="/images/icons/icon-comment2.png" alt="add reply" />
              <span class="reply-count">${post.replies.length}</span>
            </div>
          </div>
          ${currentCategory !== 3 ? `
          <div class="option-icons">
            <img class="tiny-icons" src="/images/icons/UI-more2.png" alt="more options" />
            ${myNickName !== post.nickName
        ? `<img class="tiny-icons" src="/images/icons/icon-add-friend.png" alt="add friend" data-target-id="${post.nickName}"/>`
        : ''}
            <div class="more-options-menu hidden">
              <ul>
                ${myNickName === post.nickName
        ? `
                    <li><a href="#" class="hide-post" data-post-id="${post.postId}">숨기기</a></li>
                    <hr>
                    <li><a href="#" class="delete-post" data-post-id="${post.postId}">삭제하기</a></li>
                  `
        : `<li><a href="#" class="report-post" data-post-id="${post.postId}">신고하기</a></li>`}
              </ul>
            </div>
          </div>
        ` : ''}
        `;
    return postElement;
  }

  function timeForToday(value) {
    const today = new Date();
    const timeValue = new Date(value);
    const betweenTime = Math.floor(
        (today.getTime() - timeValue.getTime()) / 1000 / 60);

    if (betweenTime < 1) {
      return '지금';
    }
    if (betweenTime < 60) {
      return `${betweenTime}분 전`;
    }

    const betweenTimeHour = Math.floor(betweenTime / 60);
    if (betweenTimeHour < 24) {
      return `${betweenTimeHour}시간 전`;
    }

    const betweenTimeDay = Math.floor(betweenTime / 60 / 24);
    if (betweenTimeDay < 30) {
      return `${betweenTimeDay}일 전`;
    }
    if (betweenTimeDay < 365) {
      return `${Math.floor(betweenTimeDay / 30)}달 전`;
    }
    return `${Math.floor(betweenTimeDay / 365)}년 전`;
  }

  function addFriendButtonsEventListener() {
    const addFriendButtons = document.querySelectorAll('img[alt="add friend"]');
    addFriendButtons.forEach(
        button => button.addEventListener("click", handleAddFriendClick));
  }

  function handleAddFriendClick() {
    const targetId = this.dataset.targetId;

    fetch(`/api/follow/${targetId}`)
    .then(response => handleFollowResponse(response, this))
    .catch(error => console.error("API 호출 중 오류 발생:", error));
  }

  function handleFollowResponse(response, button) {
    if (response.status === 201 || response.status === 200) {
      location.reload();
      button.alt = response.status === 201 ? "remove friend" : "add friend";
    } else {
      alert("wtf?");
    }
  }

  postsContainer.addEventListener('click', function (event) {
    const moreOptionsButton = event.target.closest(
        '.tiny-icons[alt="more options"]');

    if (moreOptionsButton) {
      handleMoreOptionsClick(event, moreOptionsButton);
    }
  });

  addDocumentClickEventListener();
});

function handleMoreOptionsClick(event, moreOptionsButton) {
  const optionsMenu = moreOptionsButton.closest('.option-icons').querySelector(
      '.more-options-menu');

  if (!optionsMenu) {
    return;
  }

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

function addDocumentClickEventListener() {
  document.addEventListener('click', function (event) {
    const isClickInsideMenu = event.target.closest('.more-options-menu');
    const isClickInsideButton = event.target.closest(
        '.tiny-icons[alt="more options"]');

    if (!isClickInsideMenu && !isClickInsideButton) {
      document.querySelectorAll('.more-options-menu').forEach(menu => {
        menu.classList.remove('visible');
        menu.classList.add('hidden');
      });
    }
  });
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
            alert('게시글이 숨김 처리 되었습니다.');
          } else {
            alert('게시글 숨김 처리에 실패했습니다.');
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
    button.addEventListener('click', function () {
      if (button.classList.contains('liked')) {
        return;
      }
      const postId = this.dataset.postId;

      if (postId) {
        fetch('/api/post/like/' + postId, {
          method: 'POST'
        })
        .then(response => {
          if (response.status === 200) {
            const likeCountElement = button.closest('div').querySelector(
                '.like-count');

            if (likeCountElement) {
              const currentLikes = parseInt(likeCountElement.textContent, 10);
              likeCountElement.textContent = currentLikes + 1;
              button.classList.add('liked');
            }
          }
        });
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
  reportPostButtons.forEach(button => {
    button.addEventListener('click', function () {
      const postId = this.dataset.postId;

      if (postId) {
        fetch('/api/post/report/' + postId, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({postId: postId}),
        })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            alert('게시글이 신고되었습니다.');
          } else {
            alert('게시글 신고에 실패했습니다.');
          }
        })
        .catch(error => {
          console.error('Error:', error);
          alert('요청에 실패했습니다.');
        });
      } else {
        alert('게시글 ID를 찾을 수 없습니다.');
      }
    });
  });
}

function openModal(followers) {
  const followerList = document.getElementById("followerList");

  followerList.innerHTML = "";

  if (followers.length === 0) {
    const listItem = document.createElement("li");
    listItem.classList.add("list-group-item");
    listItem.textContent = "거기 아무도 없나요..? 😥";
    followerList.appendChild(listItem);
  } else {
    followers.forEach(follower => {
      const listItem = document.createElement("li");
      listItem.classList.add("list-group-item");

      listItem.innerHTML = `
        <a href="/profile/${follower.nickname}" class="d-flex align-items-center">
          <img src="${follower.profileImage}" alt="Profile" class="rounded-circle" width="30" height="30">
          ${follower.name} (@${follower.nickname})
        </a>
      `;

      followerList.appendChild(listItem);
    });
  }

  // 모달을 표시
  const myModal = new bootstrap.Modal(document.getElementById('followerModal'));
  myModal.show();
}


function getFollowers() {
  const followerApiUrl = `/api/user/${userNickName}`;
  fetch(followerApiUrl)
  .then(response => response.json())
  .then(data => {
    const followers = data.followers || [];
    openModal(followers);
  })
  .catch(error => {
    console.error("팔로워 목록을 가져오는 데 실패했습니다.", error);
  });
}

function getFollowings() {
  const followingsApiUrl = `/api/user/${userNickName}`;
  fetch(followingsApiUrl)
  .then(response => response.json())
  .then(data => {
    const followers = data.followings || [];
    openModal(followers);
  })
  .catch(error => {
    console.error("팔로잉 목록이 Even하게 익지 않았습니다.", error);
  });
}

window.addEventListener("DOMContentLoaded", () => {
  const followerLink = document.getElementById("followerLink");
  const followingLink = document.getElementById("followingLink");
  const followBtn = document.getElementById("followOnProfileBtn");
  followerLink.addEventListener("click", (event) => {
    event.preventDefault();
    const nickname = `${userNickName}`;

    getFollowers(nickname);
  });
  followingLink.addEventListener("click", (event) => {
    event.preventDefault();
    const nickname = `${userNickName}`;

    getFollowings(nickname);
  })
  followBtn.addEventListener("click", (event) => {
    event.preventDefault();

    fetch(`/api/follow/${userNickName}`).then(() => location.reload());
  })
});
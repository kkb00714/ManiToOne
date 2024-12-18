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

  async function handleScroll() {
    if (window.innerHeight + window.scrollY >= document.body.scrollHeight
        - 100) {
      if (!isLoading && hasMorePosts) {
        isLoading = true;
        pageNum++;
        await loadPosts(pageNum);
      }
    }
  }

  async function switchCategory(category) {
    currentCategory = category;
    pageNum = 0;
    hasMorePosts = true;
    postsContainer.innerHTML = '';
    await loadPosts(pageNum);
  }

  async function loadPosts(pageNum) {
    const apiUrl = getApiUrl(pageNum);
    const menuSwitch = document.querySelector('.mypage-menu-switch');
    const postsContainer = document.getElementById("postsContainer");
    try {
      const posts = await fetchPosts(apiUrl);
      if (posts && posts.length > 0) {
        menuSwitch.style.borderBottom = '3px solid rgba(171, 171, 171, 0.25)';

        if (pageNum === 0) {
          postsContainer.innerHTML = '';
        }

        for (const post of posts) {
          const postElement = await createPostElement(post);
          postsContainer.appendChild(postElement);
        }
      } else {
        hasMorePosts = false;

        if (pageNum === 0) {
          let emptyMessage;
          if (currentCategory === 1) {
            emptyMessage = '아직 작성한 게시물이 없어요';
          } else if (currentCategory === 2) {
            emptyMessage = '아직 좋아요를 누른 게시물이 없어요';
          } else if (currentCategory === 3) {
            emptyMessage = '숨긴 게시물이 없어요';
          }

          postsContainer.innerHTML = `
                    <div class="empty-post-container">
                        <img src="/images/icons/UI-clover2.png" alt="empty post icon">
                        <div class="empty-post-message">${emptyMessage}</div>
                    </div>
                `;
        }
      }
    } catch (error) {
      console.error('게시물을 불러오지 못했습니다.:', error);
    } finally {
      isLoading = false;
    }

    addFriendButtonsEventListener();
    addHidePostEventListener();
    addReportPostEventListener();
    addDocumentClickEventListener();
    addPostLikeEventListener();
    addPostDeleteEventHandler();
    postContentEventListener();
  }

  async function fetchPosts(apiUrl) {
    const response = await fetch(apiUrl);
    if (!response.ok) {
      throw new Error('게시물 목록을 가져오지 못했습니다.');
    }
    return response.json();
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

  async function createPostElement(post) {
    const isFollowed = await getIsFollowed(post.nickName);

    const postElement = document.createElement("div");
    postElement.classList.add("post-container");

    const timeText = post.updatedAt ? `(수정됨) ${timeForToday(post.updatedAt)}`
        : timeForToday(post.createdAt);

    postElement.innerHTML = `
        <a href="/profile/${post.nickName}"><img class="user-photo" src="${post.profileImage
    || '/images/icons/UI-user2.png'}" alt="user icon" /></a>
        <div class="post-content">
          <div class="user-info">
            <a href="/profile/${post.nickName}" class="user-name">${post.nickName}</a>
            <span class="passed-time" data-created-at="${post.createdAt}" data-updated-at="${post.updatedAt}">${timeText}</span>
          </div>
          <p class="content-text" data-post-id="${post.postId}">${post.content}</p>
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
          ${myNickName !== post.nickName && !isFollowed
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
      ` : `
        <div class="option-icons">
          <img class="tiny-icons" src="/images/icons/UI-more2.png" alt="more options" />
          ${myNickName !== post.nickName && !isFollowed
        ? `<img class="tiny-icons" src="/images/icons/icon-add-friend.png" alt="add friend" data-target-id="${post.nickName}"/>`
        : ''} 
          <div class="more-options-menu hidden">
            <ul>
              ${myNickName === post.nickName
        ? `
                  <li><a href="#" class="hide-post" data-post-id="${post.postId}">숨기기 해제</a></li>
                  <hr>
                  <li><a href="#" class="delete-post" data-post-id="${post.postId}">삭제하기</a></li>
                `
        : `<li><a href="#" class="report-post" data-post-id="${post.postId}">신고하기</a></li>`}
            </ul>
          </div>
        </div>
      `}
      `;

    return postElement;
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
      const isFollowed = body === 'true';

      return isFollowed;
    } catch (error) {
      console.error("팔로우 상태를 가져오는데 실패했습니다.:", error);
      return false;
    }
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
      alert("how?");
    }
  }

  function postContentEventListener() {
    const postContents = document.querySelectorAll('.content-text');
    postContents.forEach(
        click => click.addEventListener("click", handlePostContentClick));
  }

  function handlePostContentClick() {
    const postId = this.dataset.postId;
    location.href = "/post/" + postId;
  }

  postsContainer.addEventListener('click', function (event) {
    const moreOptionsButton = event.target.closest(
        '.tiny-icons[alt="more options"]');

    if (moreOptionsButton) {
      handleMoreOptionsClick(event, moreOptionsButton);
    }
  });

  addDocumentClickEventListener();

  document.getElementById("toggle-password-sign-in").addEventListener("click",
      function () {
        const passwordInput = document.getElementById("user-password");
        const toggleButton = this;

        if (passwordInput.type === "password") {
          passwordInput.type = "text";
          toggleButton.textContent = "숨기기";
        } else {
          passwordInput.type = "password";
          toggleButton.textContent = "표시";
        }
      });

  const profileImage = document.querySelector("#user-photo");
  const currentUserProfile = document.querySelector(".user-photo");
  const profileImageInput = document.querySelector("#profile-image-input");
  const modal = document.querySelector("#image-action-modal");
  const deletePhotoBtn = document.querySelector("#delete-photo-btn");
  const uploadPhotoBtn = document.querySelector("#upload-photo-btn");
  const closeModalBtn = document.querySelector("#close-modal-btn");
  const modalOverlay = document.querySelector("#modal-overlay");

  function updateProfileImage(file) {
    const formData = new FormData();
    formData.append("file", file);

    fetch(`/api/update-profile-image`, {
      method: "POST",
      body: formData,
    })
    .then((response) => {
      if (!response.ok) {
        return response.text().then((message) => {
          throw new Error(message);
        });
      }
      return response.text();
    })
    .then((profileImageUrl) => {
      alert("프로필 이미지가 성공적으로 업데이트되었습니다.");
      // profileImage.src = profileImageUrl;
      location.reload();
    })
    .catch((error) => {
      alert(`프로필 이미지 업데이트에 실패했습니다: ${error.message}`);
    });
  }

  profileImage.addEventListener("click", () => {
    modal.style.display = "block";
    modalOverlay.style.display = "block";
  });

  const closeModal = () => {
    modal.style.display = "none";
    modalOverlay.style.display = "none";
  };

  closeModalBtn.addEventListener("click", closeModal);

  modalOverlay.addEventListener("click", closeModal);

  deletePhotoBtn.addEventListener("click", () => {
    // profileImage.src = defaultImageSrc;
    // profileImageInput.value = "";
    updateProfileImage(null);
    closeModal();
  });

  uploadPhotoBtn.addEventListener("click", () => {
    profileImageInput.click();
    closeModal();
  });

  profileImageInput.addEventListener("change", (event) => {
    const file = event.target.files[0];
    if (file) {
      if (!file.type.startsWith("image/")) {
        alert("이미지 파일만 선택할 수 있습니다.");
        profileImageInput.value = "";
        return;
      }

      // const reader = new FileReader();
      //
      // reader.onload = (e) => {
      //   profileImage.src = e.target.result;
      // };

      // reader.readAsDataURL(file);
      updateProfileImage(file);
    }
  });

  const updateProfileButton = document.getElementById('update_profile_button');

  updateProfileButton.addEventListener('click', function () {
    const nicknameInput = document.getElementById('user-nickname');
    const introduceInput = document.getElementById('user-introduce');
    const passwordInput = document.getElementById('user-password');

    const currentNickname = nicknameInput.value.trim();
    const currentIntroduce = introduceInput.value.trim();
    const currentPassword = passwordInput.value.trim();

    const userData = {};
    if (currentNickname !== nicknameInput.defaultValue && currentNickname) {
      userData.nickname = currentNickname;
    }
    if (currentIntroduce !== introduceInput.defaultValue && currentIntroduce) {
      userData.introduce = currentIntroduce;
    }
    if (currentPassword && currentPassword !== "") {
      userData.password = currentPassword;
    }

    if (Object.keys(userData).length === 0) {
      alert("변경된 내용이 없습니다.");
      return;
    }

    fetch('/api/update', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData)
    })
    .then(response => {
      if (response.ok) {
        // const nickname = userData.nickname === null ? currentNickname : userData.nickname;
        location.reload();
        closeProfileUpdateModal();
      } else {
        throw new Error('프로필 수정에 실패했습니다.');
      }
    })
    .catch(error => {
      alert(`오류가 발생했습니다: ${error.message}`);
    });
  });

  function closeProfileUpdateModal() {
    const profileUpdateModal = document.getElementById(
        'profileUpdateModalBackground');
    profileUpdateModal.style.display = 'none';

    const userNickname = document.getElementById('user-nickname').value.trim();
    window.location.href = `/profile/${userNickname}`;
  }

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
            alert('게시글 숨기기 토글이 완료되었습니다.');
          } else {
            alert('게시글 숨기기 토글에 실패했습니다.');
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
              fetch('/api/post/like/number/' + postId)
              .then(response => response.text())
              .then(countText => {
                const currentLikes = parseInt(countText, 10);
                likeCountElement.textContent = currentLikes;
                button.classList.add('liked');
              })
              .catch(error => {
                console.error('Error fetching like count:', error);
              });
            }
          }
        })
        .catch(error => {
          console.error('Error liking the post:', error);
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

  // 신고하기 버튼 클릭 시 API 요청
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

    fetch(`/api/post/report/${postId}?reportType=${selectedReportType}`, {
      method: 'POST',
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

  if (!reportSendButton.hasEventListener) {
    reportSendButton.addEventListener('click', handleReportSubmit);
    reportSendButton.hasEventListener = true;
  }

  closeReportModalButton.addEventListener('click', function () {
    reportModal.style.display = 'none';
    reportModalContainer.style.display = 'none';
  });

  window.addEventListener('click', function (event) {
    if (event.target === reportModal) {
      reportModal.style.display = 'none';
      reportModalContainer.style.display = 'none';
    }
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
          <span class="ms-2">${follower.name} (@${follower.nickname})</span>
        </a>
      `;

      followerList.appendChild(listItem);
    });
  }

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
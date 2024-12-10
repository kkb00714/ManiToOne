const Timeline = {
  page: 0,
  loading: false,
  hasMore: true,
  container: null,

  init() {
    this.container = document.querySelector('.timeline-posts');
    if (!this.container) return;

    this.setupInfiniteScroll();
    this.loadInitialPosts();
  },

  // 무한스크롤
  setupInfiniteScroll() {
    window.addEventListener('scroll', () => {
      if (this.loading || !this.hasMore) return;

      const {scrollTop, scrollHeight, clientHeight} = document.documentElement;

      if (scrollHeight - scrollTop <= cliendHeight + 100) {
        this.loadMorePosts();
      }
    });
  },

  async loadInitialPosts() {
    this.page = 0;
    this.container.innerHTML = '';
    await this.loadMorePosts();
  },

  async loadMorePosts() {
    try {
      this.loading = true;
      this.showLoader();

      const response = await fetch(`/api/timeline?page=${this.page}&size=20`);
      if (!response.ok) throw new Error('게시글을 불러오는데 실패했습니다.');

      const data = await response.json();

      if (data.content.length === 0) {
        this.hasMore = false;
        if (this.page === 0) {
          this.showEmptyState();
        }
        return;
      }

      this.renderPosts(data.content);
      this.page++;
      this.hasMore = !data.last;

    } catch (error) {
      console.error('게시글 로딩 에러 : ' + error);
      this.showError();
    } finally {
      this.loading = false;
      this.hideLoader();
    }
  },

  renderPosts(posts) {
    posts.forEach(post => {
      const postHTML = this.createPostHTML(post);
      this.container.insertAdjacentHTML('beforeend', postHTML);
    });
  },

  createPostHTML(post) {
    return `
        <div class="post-container" data-post-id="${post.postId}">
            <img
                class="user-photo"
                src="${post.profileImage || '/images/icons/UI-user2.png'}"
                alt="user icon"
            />
            <div class="post-content">
                <div class="user-info">
                    <span class="user-name">${post.nickname}</span>
                    <span class="passed-time">${post.formattedTime}</span>
                </div>
                <p class="content-text">${post.content}</p>
                ${this.createImagesHTML(post.postImages)}
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
                />
            </div>
        </div>
    `;
  },

  createImagesHTML(images) {
    if (!images || images.length === 0) return '';

    return `
        <img 
            class="post-image" 
            src="/images/upload/${images[0].fileName}" 
            alt="post image"
        />
    `;
  },

  showLoader() {
    if (!document.querySelector('.timeline-loader')) {
      const loader = document.createElement('div');
      loader.className = 'timeline-loader';
      loader.innerHTML = '로딩 중...';
      this.container.appendChild(loader);
    }
  },

  hideLoader() {
    const loader = document.querySelector('.timeline-loader');
    if (loader) loader.remove();
  },

  showEmptyState() {
    this.container.innerHTML = `
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
  },

  showError() {
    if (this.page === 0) {
      this.container.innerHTML = `
                <div class="timeline-error">
                    <p>게시물을 불러오는 중 오류가 발생했습니다.</p>
                    <button onclick="Timeline.loadInitialPosts()">다시 시도</button>
                </div>
            `;
    }
  }
};

document.addEventListener('DOMContentLoaded', () => {
  Timeline.init();
});
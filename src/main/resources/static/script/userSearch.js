document.addEventListener("DOMContentLoaded", function () {
  const searchInput = document.getElementById("search-input");
  const searchResults = document.getElementById("search-results");
  const searchSection = document.querySelector(".search-section");
  const loading = document.querySelector(".loading");

  let isFetching = false; // 서버 요청 상태
  let currentPage = 0;    // 현재 페이지
  let isLastPage = false; // 마지막 페이지 여부

  // 디바운스 유틸리티 함수
  function debounce(func, delay) {
    let timeoutId;
    return function (...args) {
      if (timeoutId) {
        clearTimeout(timeoutId); // 이전 타이머 취소
      }
      timeoutId = setTimeout(() => {
        func.apply(this, args); // delay 후 실행
      }, delay);
    };
  }

  // 서버로부터 데이터를 가져오는 함수
  async function fetchUserData(query, page) {
    isFetching = true;
    loading.style.display = "block";

    try {
      const response = await fetch(
          `/api/user/search?query=${query}&page=${page}`);
      const data = await response.json();
      isLastPage = data.last; // 마지막 페이지 여부 확인
      return data.content;    // Page 객체의 content 필드 반환
    } catch (error) {
      console.error("데이터를 가져오는 중 에러 발생:", error);
      return [];
    } finally {
      isFetching = false;
      loading.style.display = "none";
    }
  }

  // 결과를 렌더링하는 함수
  function renderUsers(users) {
    users.forEach(user => {
      const userItem = document.createElement("div");
      userItem.classList.add("result-item");

      userItem.innerHTML = `
        <img class="user-search-photo" src="${user.profileImage}" alt="${user.nickname}" />
        <div class="nick-name">${user.nickname}</div>
      `;

      searchResults.appendChild(userItem);
    });
  }

  // 스크롤이 끝에 도달했는지 확인하는 함수
  function isScrollAtBottom() {
    return (
        searchResults.scrollHeight - searchResults.scrollTop
        === searchResults.clientHeight
    );
  }

  // 검색 입력 이벤트에 디바운스 적용
  const debouncedSearch = debounce(async function () {
    const query = searchInput.value.trim();

    if (query !== "") {
      searchResults.style.display = "flex"; // 결과창 보여주기
      searchSection.style.borderRadius = "20px 20px 0 0"; // 위쪽 둥글게

      searchResults.innerHTML = ""; // 이전 결과 초기화
      currentPage = 0;

      const users = await fetchUserData(query, currentPage);
      renderUsers(users);
    } else {
      searchResults.style.display = "none"; // 결과창 숨기기
      searchSection.style.borderRadius = "20px"; // 전체 둥글게
    }
  }, 300); // 300ms 지연

// 디바운스된 이벤트 핸들러 연결
  searchInput.addEventListener("keyup", debouncedSearch);

// 입력 필드 외 클릭 시 결과창 숨기기
  document.addEventListener("click", function (event) {
    if (!searchInput.contains(event.target) && !searchResults.contains(
        event.target)) {
      searchResults.style.display = "none";
      searchSection.style.borderRadius = "20px"; // 원래 상태로 복구
    }
  });

// 스크롤 이벤트
  // 스크롤 이벤트
  searchResults.addEventListener("scroll", async function () {
    // 스크롤이 끝에 도달했는지, 현재 서버 요청 중이 아닌지, 마지막 페이지가 아닌지 확인
    if (isScrollAtBottom() && !isFetching && !isLastPage) {
      const query = searchInput.value.trim();

      if (query !== "") {
        currentPage++;
        const users = await fetchUserData(query, currentPage);
        renderUsers(users);
      }
    }
  });
});
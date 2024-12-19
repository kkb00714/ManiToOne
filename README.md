![banner](https://github.com/user-attachments/assets/c97aadb8-b9ea-4a97-b08e-9f9ff492186a)
# ManiToOne ( 24.11.22 ~ 24.12.19 )
이스트소프트 오르미 백엔드 6기 1조 파이널 프로젝트
> 공감 할 수 있는 한 마디, 소망이 담긴 게시물 하나.
>
> 모두 모여 더 밝은 내일을 만듭니다.
---
## 팀원 목록 및 개발 파트

|[김근아<br/>(조장 및 발표)](https://github.com/listoria)|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[위영석](https://github.com/WeeYoungSeok)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[김경빈](https://github.com/kkb00714)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[전원용](https://github.com/sqrt3)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[장윤종](https://github.com/yoonjong-j)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|
|---|---|---|---|---|
|프론트 HTML, CSS, JS<br/>타임라인 조회<br/>마니또 편지 답장 CRUD<br/>편지, 답장 신고<br>마니또 매칭|관리자 페이지</br>및 유저 관리<br/></br>알림 서비스 </br>(소켓)</br>Spring Security|유저 CRUD</br>OAuth2 ( Google )</br></br>회원 가입 및 </br>이메일 인증</br></br>로컬 로그인|프로필 페이지</br>(내 프로필, 타 유저)<br/></br>유저 팔로우<br/></br>게시물 목록, </br>좋아요 한 게시물 </br>목록 가져오기|게시물 CRUD</br>게시물 좋아요 기능</br>게시물 숨기기 기능</br>답글 CRUD</br>답글 신고</br>답글 숨기기|

## 사용된 기술 스택
### Backend
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-133700?style=for-the-badge&logo=gradle&logoColor=white)
![Spring Boot](https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-341718?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
  
### Frontend
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
  
### Infrastructure
![AWS EC2](https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazon-ec2&logoColor=white)
![AWS S3](https://img.shields.io/badge/Amazon%20S3-569A31?style=for-the-badge&logo=amazon-s3&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazon-rds&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS%20CODE_DEPLOY-345678?style=for-the-badge)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

---
## 프로젝트 소개 및 주요 기능
💡 본 프로젝트는 사용자가 자유롭게 콘텐츠를 업로드할 수 있는 간단한 SNS 플랫폼을 개발하는 것을 목표로 합니다.
기본적인 SNS 기능을 구현하는 동시에, 다음과 같은 참신한 기능을 통해 새로운 사용자 경험을 제공합니다.

1. **AI 기반 피드백 기능**

    사용자가 올린 **게시글**에 대해 AI가 1차적으로 분석하여 감정에 따라 적절한 노래 추천이나 위로의 글귀를 댓글이나 알림으로 제공함으로써, 더 따뜻한 소통이 이루어질 수 있도록 지원합니다.
    
2. **마니또 게시글 기능**
    - 사용자가 **게시글**을 작성할 때 마니또 **게시글**로 지정하면, 마니또 매칭을 요청한 유저에게 **게시글**이 랜덤으로 배정됩니다.
    - 부적절한 **게시글** 걸러내기 위해 작성시 글자수 제한, 배정시 AI 검증 과정을 거친 뒤 전송됩니다.
    - 마니또 **게시글**을 받은 유저는 편지(답글)를 보낼 수 있으며, 마니또 **게시글** 작성자에게 전달됩니다.
    - 마니또 편지는 익명성을 유지하며, **게시글** 주인이 선택적으로 자신의 해당 게시물 아래에 공개할 수 있습니다.

본 SNS 플랫폼은 단순한 **게시글** 업로드를 넘어 사용자 간 1:1 소통과 피드백을 유도함으로써 소통의 의무감을 부여하고, 보다 따뜻한 온라인 커뮤니티 문화를 조성하는 데 기여하고자 합니다.

---
<details><summary>요구사항 명세 보기</summary>

### 📕 유저 관리

| 요구사항 ID | 요구사항명 | 요구사항 상세 설명 | 담당자 | 비고 |
| --- | --- | --- | --- | --- |
| U-REQ-001 | 회원가입 | 유저가 회원가입을 할 수 있다.<br/> Email(ID), Password, Name, Nickname, Birth를 필요로 하며,<br/> Email 인증을 진행한 이후 회원가입이 가능하다.<br/> ProfileImage, Introduce는 defalt 값으로 지정한다. | 김경빈 |  |
| U-REQ-002 | 이메일 인증 | 유저가 회원가입 하기 이전에 이메일을 인증을 해야 회원가입이 가능하다.<br/> Email(ID)로 발송한 인증 코드를 입력하면 인증이 완료된다. | 김경빈 |  |
| U-REQ-003 | PW 찾기 | 사용자 이메일과 이름을 받아 PW 찾기를 진행한다.<br/> Email 인증 후, 해당 Email로 임시 비밀번호를 발급한다. | 김경빈 |  |
| U-REQ-004 | 로그인(일반) | 일반 회원의 경우 아이디와 비밀번호로 로그인 할 수 있도록 한다. | 김경빈 |  |
| U-REQ-005 | 로그인(소셜) | 소셜 회원의 경우, 카카오 및 구글 회원가입을 통해 로그인 할 수 있도록 한다. | 김경빈 |  |
| U-REQ-006 | 로그아웃 | 해당 유저를 로그아웃 처리한다. | 김경빈 |  |
| U-REQ-007 | 회원 정보 수정 | 유저가 정보를 변경할 수 있도록 한다.<br/> Password, Nickname, ProfileImage, introduce만 변경할 수 있도록 한다.  | 김경빈 |                        |
| U-REQ-008 | 회원 탈퇴 | 유저가 사이트에 대한 탈퇴를 할 수 있도록 한다.<br/> 유저가 비활성화 되면, 해당 유저의 게시글 및 댓글 전부 비가시화 한다. | 김경빈 |  |
| U-REQ-009 | 팔로우 기능 | 유저가 다른 유저를 팔로우 할 수 있다.<br/> 다른 유저의 프로필에 접근하여 팔로우 버튼을 누름으로써 팔로우를 한다.<br/> 내가 팔로우하는 사용자, 나를 팔로우하는 사용자를 조회할 수 있다. | 전원용 |  |
| U-REQ-010 | 마이페이지 | 현재 로그인 중인 유저의 게시글과 간단한 정보를 조회할 수 있다.<br/> 간단한 정보 : Nickname, Introduce, ProfileImage, 팔로잉 & 팔로워 수<br/> 1. 내 게시글 보기 (썸네일 출력)<br/> 2. 내가 좋아요한 게시글 보기<br/> 3. 내가 팔로잉 한 유저 보기 (해당 유저의 프로필을 누르면 그 유저의 프로필 페이지로 이동하도록)<br/> 4. 내가 숨김처리 한 게시글 목록 보기 | 전원용 |     |
| U-REQ-011 | 알림 | 팔로우, 댓글, 좋아요, 마니또 댓글 등에 대한 알림을 받는다.<br/> 알림을 왼쪽 nav바에서 알림페이지를 통해 확인할 수 있다. | 위영석 |  |
| U-REQ-012 | 타 유저 페이지 | 다른 유저의 프로필 페이지로 이동한다.<br/> 다른 유저의 프로필에서는 ‘전체 게시글 보기’, ‘좋아요 한 게시글 보기’가 가능하다.<br/> 다른 유저의 프로필에서는 팔로우 및 언팔로우를 할 수 있다.<br/> 다른 유저의 팔로워 및 팔로잉 목록을 볼 수 있다. | 전원용 |  |

### 📙 관리자 페이지

| 요구사항 ID | 요구사항명 | 요구사항 상세 설명 | 담당자 | 비고 |
| --- | --- | --- | --- | --- |
| A-REQ-001 | 회원 조회 | 전체 유저를 조회할 수 있다. | 위영석 |  |
| A-REQ-002 | 회원 정지 | 해당 유저를 정지시킬 수 있다. | 위영석 | 한 번 신고된 유저는 경고 조치,<br/> 두 번 신고된 유저는 7일 정지,<br/> 세 번 신고된 유저는 회원 정지.  |
| A-REQ-003 | 전체 게시글 조회 | 전체 게시글을 조회할 수 있다. | 위영석 |  |
| A-REQ-004 | 특정 게시글 조회 | 특정 게시글을 조회할 수 있다.<br/> 유저 Email, 유저 닉네임, 게시글 내용으로 검색할 수 있다. | 위영석 |  |
| A-REQ-005 | 게시글 관리 | 관리자가 게시글을 삭제하거나 숨길 수 있다. | 위영석 |  |
| A-REQ-006 | 신고된 게시글 조회 | 신고된 게시글을 조회할 수 있다 | 위영석 |  |
| A-REQ-007 | 신고된 댓글 조회 | 신고된 댓글을 조회할 수 있다. | 위영석 |  |
| A-REQ-008 | 신고된 마니또 조회 | 신고된 마니또 답글을 조회할 수 있다. | 위영석 |  |
| A-REQ-009 | 신고된 게시글 관리 | 신고된 게시글을 삭제하거나 블라인드 처리할 수 있다. | 위영석 |  |
| A-REQ-010 | 신고된 댓글 관리 | 신고된 댓글을 삭제하거나 블라인드 처리할 수 있다. | 위영석 |  |

- ### 📗 게시글

| 요구사항 ID | 요구사항명 | 요구사항 상세 설명 | 담당자 | 비고 |
| --- | --- | --- | --- | --- |
| F-REQ-001 | 타임라인 조회 | 메인 페이지에 타임라인을 출력한다.<br>게시글은 현재 날짜를 기준으로 최대 7일까지의 게시글(랜덤)과 팔로우한 유저의 게시글(우선)을 출력한다.<br>타임라인에서 좋아요, 답글 작성, 숨기기, 삭제 등을 할 수 있다. | 김근아 |  |
| F-REQ-002 | 게시글 생성 | 현재 로그인한 유저가 게시글을생성한다.<br/> 사진 (최대 4장), 내용을 작성할 수 있다.<br/> 마니또 게시글로 생성할 경우, 앨런AI를 사용하여 해당 게시글이 적절한지 검사한 후 적절하다면 전송한다. | 장윤종 |  |
| F-REQ-003 | 게시글 수정 | 현재 로그인한 유저가 해당 게시글을 수정할 수 있다.<br/> 사진, 내용을 수정할 수 있다. | 장윤종 |  |
| F-REQ-004 | 게시글 삭제 | 현재 로그인한 유저가 게시글을 삭제한다. | 장윤종 |  |
| F-REQ-005 | 게시글 숨기기 | 현재 로그인한 유저가 해당 게시글을 숨긴다. | 장윤종 |  |
| F-REQ-006 | 게시글 좋아요 | 게시글에 대해 ‘좋아요’ 할 수 있다. | 장윤종 |  |
| F-REQ-008 | 신고 | 적절하지 않은 게시글, 답글을 신고할 수 있다.<br/> 신고된 게시글, 답글은 신고 횟수가 일정량 이상이면 숨김 처리가 된다. | 장윤종 |  |
| F-REQ-009 | 게시글 가져오기 | 특정 유저가 작성한 게시글을 모두 가져온다. <br/> 숨김처리된 게시글은 제외하고 작성했던 게시글을 가져온다. | 전원용 |  |
| F-REQ-010 | 좋아요 게시글 가져오기 | 특정 유저가 좋아요 한 게시글을 모두 가져온다. | 전원용 |  |
| F_REQ-011 | 게시글 상세 조회 | 게시글을 누르면 게시글 상세 화면으로 이동한다. | 장윤종 |  |
| F_REQ-012 | 게시글 조회 | 유저가 생성한 게시글을 조회한다. | 장윤종 |  |

### 📘 답글

| 요구사항 ID | 요구사항명 | 요구사항 상세 설명 | 담당자 | 비고 |
| --- | --- | --- | --- | --- |
| C-REQ-001 | 답글 생성 | 현재 유저가 게시글에 답글(게시글)을 생성한다. | 장윤종 |  |
| C-REQ-002 | 답글의 답글 생성 | 현재 유저가 게시글에 남겨진 답글에 답글을 생성한다. | 장윤종 |  |
| C-REQ-003 | 답글 수정 | 자신이 작성한 답글만 수정할 수 있다. | 장윤종 |  |
| C-REQ-004 | 답글 삭제 | 자신이 작성한 답글만 삭제할 수 있다. | 장윤종 |  |
| C-REQ-005 | 답글 신고 | 적절하지 않은 답글을 신고할 수 있다. | 장윤종 |  |
| C-REQ-006 | 답글 숨기기 | 현재 로그인한 유저가 해당 답글을 숨긴다. | 장윤종 |  |

### 🍀 마니또

| 요구사항 ID | 요구사항명 | 요구사항 상세 설명 | 담당자 | 비고 |
| --- | --- | --- | --- | --- |
| M-REQ-001 | 마니또 편지 | 마니또 게시글에 대한 편지를 남길 수 있다. | 김근아 |  |
| M-REQ-002 | 마니또 편지 신고 | 적절하지 않은 마니또 편지를 받을 경우 신고할 수 있다. | 김근아 |  |
| M-REQ-003 | 마니또에게 답장 보내기 | 편지를 보내준 마니또에게 답장을 보낼 수 있다. | 김근아 |  |
| M-REQ-004 | 마니또 답장 신고 | 적절하지 않은 마니또 답장을 받을 경우 신고할 수 있다. | 김근아 |  |
| M-REQ-005 | 마니또 편지 공개 여부 | 게시글에 남긴 편지를 타 유저에게 공개할지 결정할 수 있다. | 김근아 |  |
| M-REQ-006 | 받은 마니또 편지 보기 | 편지함에서 내가 받은 마니또 편지를 볼 수 있다.  | 김근아 |  |
| M-REQ-007 | 보낸 마니또 편지 보기 | 편지함에서 내가 보낸 마니또 편지를 볼 수 있다. | 김근아 |  |
| M-REQ-008 | 받은 마니또 답장 보기 | 편지함에서 내가 받은 마니또 답장을 볼 수 있다. | 김근아 |  |
| M-REQ-009 | 보낸 마니또 답장 보기 | 편지함에서 내가 보낸 마니또 답장을 볼 수 있다. | 김근아 |  |
| M-REQ-010 | 마니또 배정 | 마니또 페이지를 들어가 마니또를 배정 받을 수 있다. | 김근아 |  |
| M-REQ-011 | 게시글 넘기기 | 배정받은 마니또가 마음에 들지 않으면 넘길 수 있다. | 김근아 |  |

### 📕 AI

| 요구사항 ID | 요구사항명 | 요구사항 상세 설명 | 담당자 | 비고 |
| --- | --- | --- | --- | --- |
| M-REQ-001 | AI 피드백 | 게시글 작성시 AI 피드백과 노래 추천을 받을 수 있다. | 전원용 |  |
| M-REQ-002 | 게시글 검증  | 특정 게시글을 작성할 떄 AI가 부적절한 내용인지 감지를 한다.<br>-부적절한 내용일 경우 마니또 게시글로 배정되지 않는다. | 전원용, 김근아 |  |

</details>

---


<details><summary> API 명세서 보기
</summary>

### [ 📖 유저 관리 ]

| 서비스명 | 메서드 | URL | 설명 |
| --- | --- | --- | --- |
| registerUser | POST | /api/signup | 회원가입 |
| verifyEmail | POST | /api/email-validate | 이메일 인증 코드 발송 |
|  verifyNumber | POST | /api/email-check | 이메일 인증 코드 인증 |
| isExist | GET | /api/exist-email-and-nick | 유저 이메일, 닉네임 중복 검사 |
| findPassword | POST | /api/password-reset | 비밀번호 찾기 |
| localLogin | POST | /api/local-login | 로그인(일반) |
| socialLogin | POST | /api/oauth-login | 로그인(OAuth) |
| updateAdditionalInfo | PUT | /api/additional-info | OAuth 회원가입 시 필요한 데이터 입력 |
| logout | GET | /api/logout | 로그아웃 |
| deleteUser | DELETE | /api/cancel-account | 회원 탈퇴 |
| updateUser | PUT | /api/user | 회원 수정 |
| follow | POST | /api/follow | 팔로우 (토글) |
| getFollower | GET | /api/follow/{nickname} | 팔로워, 팔로잉 조회 |

---

### [ 📖 알림 ]

| 서비스명 | 메서드 | URL | 설명 |
| --- | --- | --- | --- |
| readNotification | PUT | /api/notification/{notiId} | 알림 읽음 처리 |
| checkUnreadNotifications | GET | /api/notifications/status | 해당 유저의 알림 읽음 상태 가져오기<br/>(읽지 않은 알림이 있다면 true, 없다면 false) |
| readAllNotification | PUT | /api/notification | 알림 모두 읽음 처리 |

---

### [ 📖 관리자 페이지 ]

| 서비스명 | 메서드 | URL | 설명 |
| --- | --- | --- | --- |
| getAllUsers | GET | /admin/api/users?type={type}&content={content}&status={status}&page={page} | 회원 조회<br/>String : type, content<br/>Integer : status, page |
| updateUser | PUT | /admin/api/users | 회원 정보 수정 |
| updateUserProfileImage | PUT | /admin/api/users/{userId} | 유저 프로필 사진 업데이트<br/>(userProfileFile은 RequestPart) |
| getAllPosts | GET | /admin/api/posts?type={type}<br/>&content={content}<br/>&isBlind={isBlind}<br/>&page={page} | 전체 게시글 조회<br/>String : type, content<br/>Integer : page<br/>Boolean : isBlind |
| getPostImages | GET | /admin/api/post/{postId}/image | 게시글 이미지 가져오기 |
| blindPost | PUT | /admin/api/blind/post/{postId} | 게시글 블라인드 처리 |
| blindReply | PUT | /admin/api/blind/reply/{replyPostId} | 댓글 블라인드 처리 |
| deletePost | DELETE | /admin/api/post/{postId} | 게시글 삭제 처리 |
| deleteReply | DELETE | /admin/api/reply/{replyPostId} | 댓글 삭제 처리 |
| getReports | POST | /admin/api/reports?type={type}<br/>&content={content}<br/>&reportObjectType={reportObjectType}<br/>&reportType={reportType}<br/>&page={page} | 신고 된 게시글, 댓글 조회<br/>String : type, content, reportObjectType, reportType<br/>Integer : page |
| isReportPost | GET | /admin/api/report/post/{postId} | 신고된 게시글인지 확인 |
| isReportReply | GET | /admin/api/report/reply/{replyPostId} | 신고된 댓글인지 확인 |
| deleteReport | DELETE | /admin/api/report/{reportId} | 신고 삭제 처리 |
| getReportedManitos | GET | /admin/api/report/manitos?type={type}&content={content}&reportObjectType={reportObjectType}&reportType={reportType}&page={page} | 신고된 마니또 답글 조회<br/>String : type, content, reportObjectType, reportType<br/>Integer : page |

---

### [ 📖 게시글 ]

| 서비스명 | 메서드 | URL | 설명 |
| --- | --- | --- | --- |
| createPost | POST | /api/post | 게시글 생성 |
| getPosts | GET | /api/posts | 게시글 조회 |
| getPostDetail | GET | /api/post/{postId} | 게시글 상세 조회 |
| updatePost | PUT | /api/post/{postId} | 게시글 수정 |
| deletePost | DELETE | /api/post/{postId} | 게시글 삭제 |
| hidePost | PUT | /api/post/hidden/{postId} | 게시글 숨기기 |
| getMyHiddenPost | GET | /api/post/hidden | 내 숨김 게시글 가져오기 |
| likePost | POST | /api/post/like/{postId} | 게시글 좋아요 |
| reportPost | POST | /api/post/report/{postId} | 게시글 신고 |
| getPostsByNickname | GET | /api/posts/by/{nickname} | 게시글 가져오기 |
| getLikePostsByNickname | GET | /api/posts/{nickname}/liked | 좋아요 게시글 가져오기 |
| getPostLikesNum | GET | /api/post/like/number/{postId} | 게시글 좋아요 개수 조회 |
| getFeedback | GET | /api/post/ai-feedback | 게시글 AI 피드백 받기 |
| getHiddenPost | GET | /api/post/hidden | 숨긴 게시글 가져오기 |

---

### [ 📖 답글 ]

| 서비스명 | 메서드 | URL | 설명 |
| --- | --- | --- | --- |
| createReply | POST | /api/reply/{postId} | 답글 생성 |
| createReReply | POST | /api/rereply/{replyId} | 답글의 답글 생성 |
| updateReply | PUT | /api/reply/{replyPostId} | 답글 수정 |
| deleteReply | DELETE | /api/reply/{replyId} | 답글 삭제 |
| likeReply | POST | /api/reply/like/{replyId} | 답글 좋아요 |
| reportReply | POST | /api/reply/report/{replyId} | 답글 신고 |
| hideReply | PUT | /api/reply/hidden/{replyId} | 답글 숨기기 |
| getReplies | GET | /api/replies/{postId} | 답글 조회 |
| getReply | GET | /api/reply/{replyId} | 답글 단건 조회 |
| getReReplies | GET | /api/rereplies/{replyId} | 답글의 답글 조회 |
| getRepliesNum | GET | /api/replies/number/{postId} | 답글 개수 조회 |
| getReRepliesNum | GET | /api/rereplies/number/{replyId} | 답글의 답글 개수 조회 |
| getReplyLikesNum | GET | /api/reply/like/number/{replyId} | 답글 좋아요 개수 조회 |

---

### [ 📖 마니또 ]

| 서비스명 | 메서드 | URL | 설명 |
| --- | --- | --- | --- |
| createManitoReply | POST | /api/manito/letter/{manitoMatchesId} | 마니또 편지 작성 |
| answerManitoReply | PUT | /api/manito/anwer/{manitoPostId} | 마니또 편지 답장 |
| reportManitoReply | PUT | /api/manito/report/{manitoPostId} | 마니또 편지 신고 |
| reportManitoAnswer | PUT | /api/manito/report/answer{manitoPostId} | 마니또 편지 답장 신고 |
| hideManitoReply | PUT | /api/manito/hide/letter/{manitoPostId} | 마니또 편지 공개 여부 |
| getReceiveManito | GET | /api/receivemanito/{nickname} | 받은 마니또 편지 |
| getSendManito | GET | /api/sendmanito/{nickname} | 보낸 마니또 편지 |
| createMatch | POST | /api/manito/match | 마니또 매칭 |
| passManitoMatch | PUT | /api/manito/pass/{matitoMatchesId} | 매칭된 마니또 패스 |

</details>

---
## ERD
[![image](https://github.com/user-attachments/assets/d57b9319-9ff9-4a75-a0ae-b25b647d176b)](https://www.erdcloud.com/d/na7YdajKCyZNxrxEB)
이미지를 클릭 시 페이지로 이동합니다.

---
## Figma
[![image](https://github.com/user-attachments/assets/1e8b23df-5865-47ca-b4b4-4288b055527c)](https://www.figma.com/design/c48u4PFZcGRbTgB3qTGRM8/%ED%8F%AC%EC%8A%A4%ED%8A%B8%EC%9E%87's-team-library?node-id=3311-3&p=f&t=QuA58hv1eizWh0P7-0)
이미지를 클릭 시 페이지로 이동합니다.

package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.ManitoComment;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ManitoCommentRepository extends JpaRepository<ManitoComment, Long> {

  Page<ManitoComment> findByPostId_User_Nickname(String nickname, Pageable pageable);

  Page<ManitoComment> findByUser_Nickname(String nickname, Pageable pageable);

  Optional<ManitoComment> findByPostIdAndUser(Post post, User user);
}

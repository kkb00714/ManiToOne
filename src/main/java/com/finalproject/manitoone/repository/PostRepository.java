package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  Optional<List<Post>> findAllByIsBlindFalseAndIsHiddenFalseAndUser_Nickname(String nickName,
      Pageable pageable);

  Optional<List<Post>> findAllByIsBlindFalseAndIsHiddenTrueAndUser_Nickname(String nickName,
      Pageable pageable);

  Optional<Post> findByPostId(Long postId);

  Optional<List<Post>> findAllByPostId(Long postId);
}

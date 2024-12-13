package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.ManitoLetter;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManitoLetterRepository extends JpaRepository<ManitoLetter, Long> {

  Page<ManitoLetter> findByPostId_User_Nickname(String nickname, Pageable pageable);

  Page<ManitoLetter> findByUser_Nickname(String nickname, Pageable pageable);

  Optional<ManitoLetter> findByPostIdAndUser(Post post, User user);

  Optional<List<ManitoLetter>> findAllByPostIdPostId(Long postId);

  Optional<ManitoLetter> findByPostIdPostIdAndUserNickname(Long postId, String nickname);

  Optional<ManitoLetter> findByPostId(Post post);

}

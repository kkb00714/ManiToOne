package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.ManitoLetter;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManitoLetterRepository extends JpaRepository<ManitoLetter, Long> {

  Optional<ManitoLetter> findByManitoMatches_ManitoMatchesId(Long manitoMatchesId);

  Page<ManitoLetter> findByManitoMatches_MatchedPostId_User_Nickname(String nickname,
      Pageable pageable);

  Page<ManitoLetter> findByManitoMatches_MatchedUserId_Nickname(String nickname, Pageable pageable);

}

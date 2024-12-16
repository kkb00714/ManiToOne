package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.dto.user.MainUserSearchResponseDto;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findUserByNickname(String nickname);

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndName(String email, String name);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  User findOAuth2ByEmail(String email);

  @Query("SELECT new com.finalproject.manitoone.dto.user.MainUserSearchResponseDto(u.profileImage, u.nickname) " +
      "FROM User u WHERE u.nickname LIKE :query%")
  Page<MainUserSearchResponseDto> findUsersByNicknameStartsWith(@Param("query") String query, Pageable pageable);
}
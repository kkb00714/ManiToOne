package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findUserByNickname(String nickname);

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndName(String email, String name);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);
}
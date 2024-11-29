package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.Follow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

  Optional<List<Follow>> findAllByFollower_UserId(Long userId);

  Optional<List<Follow>> findAllByFollowing_UserId(Long userId);
}

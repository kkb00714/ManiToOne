package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.PostImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

  Optional<List<PostImage>> findAllByPost_PostId(Long postId);
}

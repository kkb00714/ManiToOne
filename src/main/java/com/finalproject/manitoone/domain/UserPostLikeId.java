package com.finalproject.manitoone.domain;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class UserPostLikeId implements Serializable {

  private Long user; // User 엔티티의 user_id
  private Long post; // Post 엔티티의 post_id

  // equals()와 hashCode()를 반드시 구현해야 JPA가 올바르게 동작
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserPostLikeId that = (UserPostLikeId) o;
    return Objects.equals(user, that.user) &&
        Objects.equals(post, that.post);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, post);
  }
}
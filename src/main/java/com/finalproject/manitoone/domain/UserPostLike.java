package com.finalproject.manitoone.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_post_like")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserPostLikeId.class)
public class UserPostLike {

  @Id
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Id
  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

//  @Id
//  @ManyToOne
//  @JoinColumn(name = "reply_post_id", nullable = false)
//  private ReplyPost replyPost;
}
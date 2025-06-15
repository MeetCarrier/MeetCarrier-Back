package com.kslj.mannam.domain.review.repository;

import com.kslj.mannam.domain.review.entity.Review;
import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer WHERE r.user = :user")
    List<Review> findReviewByUser(@Param("user") User user);

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.reviewer.id = :reviewerId")
    List<Review> findReviewByReviewerId(@Param("reviewerId") Long reviewerId);

    boolean existsByReviewer_IdAndUser_Id(Long reviewerId, Long userId);
}

package com.kslj.mannam.domain.review.repository;

import com.kslj.mannam.domain.review.entity.Review;
import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findReviewByUser(User user);
    List<Review> findReviewByReviewerId(long reviewerId);
}

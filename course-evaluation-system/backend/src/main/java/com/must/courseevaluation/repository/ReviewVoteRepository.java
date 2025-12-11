package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
    
    /**
     * 查找用户对某条评论的投票
     */
    Optional<ReviewVote> findByReviewIdAndUserId(Long reviewId, Long userId);
    
    /**
     * 统计某条评论指定类型的投票数量
     */
    Long countByReviewIdAndVoteType(Long reviewId, ReviewVote.VoteType voteType);
    
    /**
     * 删除用户对某条评论的投票
     */
    void deleteByReviewIdAndUserId(Long reviewId, Long userId);
    
    /**
     * 检查用户是否已投票
     */
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
}


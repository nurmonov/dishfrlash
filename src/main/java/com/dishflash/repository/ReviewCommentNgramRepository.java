package com.dishflash.repository;

import com.dishflash.entity.ReviewCommentNgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCommentNgramRepository extends JpaRepository<ReviewCommentNgram, Long> {

    List<ReviewCommentNgram> findByNgramBlindIndexIn(List<String> ngramBlindIndices);

    @Modifying
    @Query("DELETE FROM ReviewCommentNgram n WHERE n.review.id = :reviewId")
    void deleteByReviewId(Long reviewId);
}

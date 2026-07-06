package com.dishflash.repository;

import com.dishflash.entity.CategoryNameNgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryNameNgramRepository extends JpaRepository<CategoryNameNgram, Long> {

    List<CategoryNameNgram> findByNgramBlindIndexIn(List<String> ngramBlindIndices);

    @Modifying
    @Query("DELETE FROM CategoryNameNgram n WHERE n.category.id = :categoryId")
    void deleteByCategoryId(Long categoryId);
}

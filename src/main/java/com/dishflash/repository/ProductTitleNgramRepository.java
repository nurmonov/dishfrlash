package com.dishflash.repository;

import com.dishflash.entity.ProductTitleNgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTitleNgramRepository extends JpaRepository<ProductTitleNgram, Long> {

    List<ProductTitleNgram> findByNgramBlindIndexIn(List<String> ngramBlindIndices);

    @Modifying
    @Query("DELETE FROM ProductTitleNgram n WHERE n.product.id = :productId")
    void deleteByProductId(Long productId);
}

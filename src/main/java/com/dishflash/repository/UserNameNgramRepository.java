package com.dishflash.repository;

import com.dishflash.entity.UserNameNgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNameNgramRepository extends JpaRepository<UserNameNgram, Long> {

    List<UserNameNgram> findByNgramBlindIndexIn(List<String> ngramBlindIndices);

    @Modifying
    @Query("DELETE FROM UserNameNgram n WHERE n.user.id = :userId")
    void deleteByUserId(Long userId);
}

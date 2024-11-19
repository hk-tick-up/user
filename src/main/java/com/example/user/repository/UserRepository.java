package com.example.user.repository;

import com.example.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByNickname(String nickname);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.deleteRequestAt = :deleteRequestAt WHERE u.id = :id")
    void updateDeleteRequestAt(@Param("id") String id,
                               @Param("deleteRequestAt") LocalDateTime deleteRequestAt);

    boolean existsByNickname(String nickname);
}

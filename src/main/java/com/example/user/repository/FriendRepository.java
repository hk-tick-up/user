package com.example.user.repository;

import com.example.user.entity.Friend;
import com.example.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findFriendByUserAndFriend(User user, User friend);

    List<Friend> getFriendsByUserAndStatus(User user, Friend.Status status);

    boolean existsByUserAndFriendAndStatus(User user, User friend, Friend.Status status);

    Optional<Friend> findFriendByUserAndFriendAndStatus(User user, User friend, Friend.Status status);

    void deleteFriendByUserAndFriendAndStatus(User user, User friend, Friend.Status status);

    Optional<Friend> deleteFriendByIdAndStatus(Long id, Friend.Status status);
}

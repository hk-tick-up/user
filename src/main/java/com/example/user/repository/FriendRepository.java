package com.example.user.repository;

import com.example.user.entity.Friend;
import com.example.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> getFriendsByUser(String userId);

    List<Friend> getFriendByUser(User user);


    // userId를 받아서 해당 유저에게 온 친구 요청 목록 조회
    List<Friend> findByFriendIdAndStatus(String friendId, Friend.Status status);

    // 또는 더 명확하게
    @Query("SELECT f FROM Friend f WHERE f.friendId = :friendId AND f.status = :status")
    List<Friend> findFriendRequestsByFriendId(@Param("friendId") String friendId, @Param("status") Friend.Status status);
}

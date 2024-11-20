package com.example.user.dto;

import com.example.user.entity.Friend;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendDTO extends UserNameDTO{
    private Friend.Status status;

    public FriendDTO(String id, String nickname, Friend.Status status) {
        super(id, nickname);
        this.status = status;
    }
}

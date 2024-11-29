package com.example.user.controller;

import com.example.user.dto.FriendDTO;
import com.example.user.dto.UserNameDTO;
import com.example.user.dto.UserSigninDTO;
import com.example.user.dto.UserSignupDTO;
import com.example.user.entity.User;
import com.example.user.service.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserServiceImpl userService;
    private final DefaultAuthenticationEventPublisher authenticationEventPublisher;

    @PostMapping("/sign-up")
    public String signUp(@RequestBody UserSignupDTO user) {
        return userService.signUp(user);
    }
    @PostMapping("/sign-in")
    public String signIn(@RequestBody UserSigninDTO user) {
        return userService.signIn(user);
    }
    @GetMapping("/self")
    public UserNameDTO self(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        return userService.self(authentication, request, response);
    }
    @GetMapping("/point")
    public int point(Authentication authentication) {
        return userService.point(authentication);
    }
    @PostMapping("/verifypassword")
    public boolean verifyPassword(Authentication authentication, @RequestBody Map<String, String> request) {
        return userService.verifyPassword(authentication.getName(), request.get("password"));
    }
    // 로그인, 로그아웃은 Spring Security에서 처리
    @PostMapping("/duplicateid")
    public boolean duplicateId(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        return userService.byIdExist(userId);
    }
    @PostMapping("/duplicatenickname")
    public boolean duplicateNickname(@RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        return userService.byNicknameExist(nickname);
    }
    @DeleteMapping("/withdrawal")
    public String withdrawal(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        return userService.deleteAccountRequest(authentication, request, response);
    }

    @GetMapping("/userinfo")
    public User personalInformation(Authentication authentication) {
        String userId = authentication.getName();
        return userService.personalInfromation(userId);
    }
    @PutMapping("/userinfo")
    public User updatePersonalInformation(Authentication authentication, @RequestBody Map<String, String> request) {
        String userId = authentication.getName();
        return userService.updatePersonalInfromation(userId, request);
    }
    @GetMapping("/username")
    public FriendDTO getUserFromString(Authentication authentication, @RequestParam("user") String username) {
        String userId = authentication.getName();
        return userService.getUserFromString(userId, username);
    }

    @GetMapping("/friends")
    public List<FriendDTO> friends(Authentication authentication) {
        String userId = authentication.getName();
        return userService.friends(userId);
    }
    @PostMapping("/friends")
    public FriendDTO requestNewFriend(Authentication authentication, @RequestBody Map<String, String> request) {
        String friendId = request.get("friendId");
        String userId = authentication.getName();
        return userService.requestNewFriend(userId, friendId);
    }
    @GetMapping("/friend-requests")
    public List<FriendDTO> friendRequests(Authentication authentication) {
        String userId = authentication.getName();
        return userService.friendRequests(userId);
    }
    @PostMapping("/friend-requests")
    public UserNameDTO acceptFriend(Authentication authentication, @RequestBody Map<String, String> request) {
        String friendId = request.get("friendId");
        String userId = authentication.getName();
        return userService.acceptFriend(userId, friendId);
    }
    @DeleteMapping("/friend-requests/{requestId}")
    public UserNameDTO deleteFriendRequest(Authentication authentication, @PathVariable Long requestId) {
        String userId = authentication.getName();
        return userService.deleteFriendRequest(userId, requestId);
    }
    @DeleteMapping("/friends/{friendId}")
    public UserNameDTO deleteFriend(Authentication authentication, @PathVariable String friendId) {
        String userId = authentication.getName();
        return userService.deleteFriend(userId, friendId);
    }

//    @GetMapping("/rewards")
//    public List<RewardDTO> rewards(Authentication authentication) {
//        String userId = authentication.getName();
//        return userService.rewards(userId);
//    }
//    @PostMapping("/rewards")
//    public RewardDTO buyReward(Authentication authentication, @RequestBody RewardDTO rewardDTO) {
//        String userId = authentication.getName();
//        return userService.buyReward(userId, rewardDTO);
//    }
//    @PutMapping("/rewards/profile-image")
//    public boolean updateProfileImage(Authentication authentication, @RequestBody Image image) {
//        String userId = authentication.getName();
//        return userService.updateProfileImage(userId);
//    }
//    @GetMapping("/points")
//    public int points(Authentication authentication) {
//        String userId = authentication.getName();
//        return userService.points(userId);
//    }
}

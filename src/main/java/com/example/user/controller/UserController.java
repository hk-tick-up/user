package com.example.user.controller;

import com.example.user.dto.RewardDTO;
import com.example.user.dto.UserAccountDTO;
import com.example.user.dto.UserNameDTO;
import com.example.user.entity.User;
import com.example.user.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("http://localhost:3000/api/v1/users")
public class UserController {
    private final UserServiceImpl userService;

    @PostMapping("/sign-up")
    public User signUp(@RequestBody User user) {
        return userService.signUp(user);
    }
    @PostMapping("/duplicateid")
    public void duplicateId(@RequestBody String userId) {
        userService.byIdExist(userId);
    }
    @PostMapping("/duplicatenickname")
    public void duplicateNickname(@RequestBody String nickname) {
        userService.byNicknameExist(nickname);
    }
    // 로그인, 로그아웃은 Spring Security에서 처리
//    @PostMapping("/sign-in")
//    @PostMapping("/sign-out")
    @DeleteMapping("/withdrawal")
    public void withdrawal(@RequestBody UserAccountDTO userAccountDTO) {
        userService.deleteAccountRequest(userAccountDTO);
    }

    @GetMapping("/")
    public User personalInformation(Authentication authentication) {
        String userId = authentication.getName();
        return userService.personalInfromation(userId);
    }
    @PutMapping("/")
    public User updatePersonalInformation(Authentication authentication, @RequestBody User user) {
        String userId = authentication.getName();
        return userService.updatePersonalInfromation(userId, user);
    }

    @GetMapping("/friends")
    public List<UserNameDTO> friends(Authentication authentication) {
        String userId = authentication.getName();
        return userService.friends(userId);
    }
    @PostMapping("/friends")
    public UserNameDTO requestNewFriend(Authentication authentication, @RequestBody String friendId) {
        String userId = authentication.getName();
        return userService.requestNewFriend(userId, friendId);
    }
    @GetMapping("/friend-requests")
    public List<UserNameDTO> friendRequests(Authentication authentication) {
        String userId = authentication.getName();
        return userService.friendRequests(userId);
    }
    @PostMapping("/friend-requests")
    public UserNameDTO acceptFriend(Authentication authentication, @RequestBody String friendId) {
        String userId = authentication.getName();
        return userService.acceptFriend(userId, friendId);
    }
    @DeleteMapping("/friend-requests/{requestid}")
    public UserNameDTO deleteFriendRequest(Authentication authentication, @RequestParam String friendId) {
        String userId = authentication.getName();
        return userService.deleteFriendRequest(userId, friendId);
    }
    @DeleteMapping("/friends/{friendid}")
    public UserNameDTO deleteFriend(Authentication authentication, @RequestParam String friendId) {
        String userId = authentication.getName();
        return userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/rewards")
    public List<RewardDTO> rewards(Authentication authentication) {
        String userId = authentication.getName();
        return userService.rewards(userId);
    }
    @PostMapping("/rewards")
    public RewardDTO buyReward(Authentication authentication, @RequestBody RewardDTO rewardDTO) {
        String userId = authentication.getName();
        return userService.buyReward(userId, rewardDTO);
    }
    @PutMapping("/rewards/profile-image")
    public boolean updateProfileImage(Authentication authentication, @RequestBody Image image) {
        String userId = authentication.getName();
        return userService.updateProfileImage(userId);
    }
    @GetMapping("/points")
    public int points(Authentication authentication) {
        String userId = authentication.getName();
        return userService.points(userId);
    }
}


//    @PostMapping("/api/v1/products")
//    public void save(@RequestBody Product product){
//        productService.save(product);
//    }
//    @PutMapping("/api/v1/products/{id}/selling")
//    public void sell(@PathVariable Long id){ productService.sell(id);}
//    @PutMapping("/api/v1/products/{id}/receiving")
//    public void receive(@PathVariable Long id){ productService.receive(id);}
//}


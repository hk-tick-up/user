package com.example.user.service;

import com.example.user.dto.RewardDTO;
import com.example.user.dto.UserAccountDTO;
import com.example.user.dto.UserNameDTO;
import com.example.user.entity.User;
import com.example.user.repository.FriendRepository;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private PasswordEncoder passwordEncoder;

    public User signUp(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().add(User.UserRole.ROLE_USER);

        return userRepository.save(user);
    }

    public void byIdExist(String userId) {
        Optional<User> account = userRepository.findById(userId);
        if(account.isPresent()) {
            log.info("User with id {} already exists", userId);
        }
    }

    public void byNicknameExist(String nickname) {
        Optional<User> account = userRepository.findByNickname(nickname);
        if(account.isPresent()) {
            log.info("User with nickname {} already exists", nickname);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }

    public void deleteAccountRequest(UserAccountDTO userAccountDTO) {
        Optional<User> user = userRepository.findById(userAccountDTO.getId());

        if(user.isPresent()) {
            userRepository.updateDeleteRequestAt(userAccountDTO.getId(), LocalDateTime.now());
        }
    }

    public void deleteAccount(String id) {
        // 일정 주기로 삭제 예정인 계정 탐색, 삭제
        userRepository.deleteById(id);
    }

    public User personalInfromation(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword("");
            return user;
        }
    }

    public User updatePersonalInfromation(String userId, User newUser) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User oldUser = optionalUser.get();
            newUser.setPassword(oldUser.getPassword());
            return userRepository.save(newUser);
        }
    }

    public List<UserNameDTO> friends(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            friendRepository.getFriendsByUser(userId);
            friendRepository.getFriendByUser()
        }
    }

    public UserNameDTO requestNewFriend(String userId, String friendId) {
    }

    public List<UserNameDTO> friendRequests(String userId) {
    }

    public UserNameDTO acceptFriend(String userId, String friendId) {
    }

    public UserNameDTO deleteFriendRequest(String userId, String friendId) {
    }

    public UserNameDTO deleteFriend(String userId, String friendId) {
    }

    public List<RewardDTO> rewards(String userId) {
    }

    public RewardDTO buyReward(String userId, RewardDTO rewardDTO) {
    }

    public boolean updateProfileImage(String userId) {
    }

    public int points(String userId) {
    }
}

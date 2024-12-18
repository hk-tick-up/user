package com.example.user.service;

import com.example.user.config.security.JwtTokenProvider;
import com.example.user.dto.*;
import com.example.user.entity.Friend;
import com.example.user.entity.User;
import com.example.user.repository.FriendRepository;
import com.example.user.repository.UserRepository;
import com.example.user.utility.Logout;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public String signUp(UserSignupDTO user) {
        // 중복 가입 막아야
        if(userRepository.existsById(user.id()))
            return "Error: Duplicated User ID";
        if(userRepository.existsByNickname(user.nickname()))
            return "Error: Duplicated Nickname";

        User newUser = User.builder()
                .id(user.id())
                .nickname(user.nickname())
                .password(passwordEncoder.encode(user.password()))
                .birthday(user.birthday())
                .gender(user.gender())
                .job(user.job())
                .point(0)
                .build();
        newUser.getRoles().add(User.UserRole.ROLE_USER);

        try{
            User saved = userRepository.save(newUser);
            log.info("Account {} has been created", user.id());
            return "Info: Account Created successfully";
        } catch(DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return "Error: Duplicated User";
        }
    }
    public String signIn(UserSigninDTO userDTO) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        boolean deletionRequested = user.getDeleteRequestAt() != null;

        return jwtTokenProvider.createToken(user.getId(), user.getRoles(), deletionRequested);
    }
    public UserNameDTO self(Authentication authentication, HttpServletRequest request, HttpServletResponse response){
//        log.warn(authentication.toString());
        // JwtAuthenticationToken [Principal=qwer, Credentials=[PROTECTED], Authenticated=true, Details=null, Granted Authorities=[ROLE_USER]]
//        log.warn(request.toString());
//        log.warn(response.toString());
        String id = authentication.getName();
        String nickname = userRepository.findById(id).get().getNickname();
        return new UserNameDTO(id, nickname);
    }
    public int point(Authentication authentication) {
        return userRepository.findById(authentication.getName()).get().getPoint();
    }
    public boolean verifyPassword(String id, String password) {
        User user = userRepository.findById(id).get();
        return passwordEncoder.matches(password, user.getPassword());
    }

    public boolean byIdExist(String userId) {
        return userRepository.existsById(userId);
    }

    public boolean byNicknameExist(String nickname) {
        return userRepository.existsByNickname(nickname);
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

    public String deleteAccountRequest(Authentication authentication) {
        String userId = authentication.getName();
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            // 로그인 된 계정 삭제예정으로 등록
            userRepository.updateDeleteRequestAt(userId, LocalDateTime.now());
            return "Info: Successfully Deleted Account";
        }
        else return "Error: User not found";
    }

    public LocalDateTime deleteAccountRequestedAt(Authentication authentication) {
        String userId = authentication.getName();
        Optional<User> user = userRepository.findById(userId);
        return user.map(User::getDeleteRequestAt).orElse(null);
    }

    public String cancelDeleteAccount(Authentication authentication){
        String userId = authentication.getName();
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            userRepository.updateDeleteRequestAt(userId, null);
            return "Info: Your account restored.";
        }
        else return null;
    }

    // 매일 자정 실행
    // cron = "초 분 시 일 월 요일"
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredAccounts() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(7);
        List<User> usersToDelete = userRepository.findAllByDeleteRequestAtBefore(thresholdDate);

        for (User user : usersToDelete) {
            userRepository.deleteById(user.getId());
            System.out.println("Deleted user: " + user.getId());
        }
    }

    public User personalInfromation(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword("");
            return user;
        }
        else return null;
    }

    @Transactional
    public User updatePersonalInfromation(String userId, Map<String, String> request) {
        // nickname, password 만 수정 가능
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            String password = request.get("password");
            String nickname = request.get("nickname");
            if(!password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            user.setNickname(nickname);
            try{
                return userRepository.save(user);
            } catch (DataIntegrityViolationException e) {
                log.error(e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public FriendDTO getUserFromString(String userId, String username) {
        Optional<User> optionalUser = userRepository.findByNickname(username);
        if(optionalUser.isPresent()) {
            return getFriendDTOByOptionalUser(optionalUser.get(), userId);
        }

        Optional<User> optionalUser2 = userRepository.findById(username);
        return optionalUser2.map(user -> getFriendDTOByOptionalUser(user, userId)).orElse(null);
    }
    private FriendDTO getFriendDTOByOptionalUser(User user, String userId) {
        // 친구 요청: userId: 인증계정 본인 -> user: 상대방
        if(user.getId().equals(userId)) {
            return new FriendDTO(user.getId(), user.getNickname(), Friend.Status.YOU);
        }

        Optional<Friend> friendship = friendRepository.findFriendByUserAndFriend(new User(user.getId()), new User(userId));
        return friendship.map(friend -> new FriendDTO(friend.getFriend().getId(), friend.getFriend().getNickname(), friend.getStatus()))
                .orElseGet(() -> new FriendDTO(user.getId(), user.getNickname(), Friend.Status.NOTYET));
    }

    public List<FriendDTO> friends(String userId) {
        User user = new User(userId);
        List<Friend> friendEntities = friendRepository.getFriendsByUserAndStatus(user, Friend.Status.FRIEND);
        return getFriendDTOS(friendEntities, true);
    }

    private List<FriendDTO> getFriendDTOS(List<Friend> friendEntities, boolean isSend) {
        List<FriendDTO> friendDTOs = new ArrayList<>();
        if(isSend) {
            for(Friend friendEntity : friendEntities) {
                User friend = friendEntity.getFriend();
                friendDTOs.add(
                        new FriendDTO(friend.getId(), friend.getNickname(), friendEntity.getStatus())
                );
            }
        }
        else{
            for(Friend friendEntity : friendEntities) {
                User user = friendEntity.getUser();
                friendDTOs.add(
                        new FriendDTO(user.getId(), user.getNickname(), friendEntity.getStatus())
                );
            }
        }
        return friendDTOs;
    }

    @Transactional
    public FriendDTO requestNewFriend(String userId, String friendId) {
        Optional<Friend> optionalRelationship = friendRepository.findFriendByUserAndFriendAndStatus(new User(userId), new User(friendId), Friend.Status.REQUEST);
        if(optionalRelationship.isPresent()) {
            Friend relationship = optionalRelationship.get();

            // 상대방이 이미 친추 요청했을 때
            if(friendRepository.existsByUserAndFriendAndStatus(relationship.getFriend(), relationship.getUser(), Friend.Status.REQUEST)){
                // 서로 친추
                return acceptFriend(userId, friendId);
            }
            else{
                // 처리 X
                return new FriendDTO(relationship.getFriend().getId(), relationship.getFriend().getNickname(), relationship.getStatus());
            }
        }
        else{
            Friend relationship = new Friend();
            relationship.setUser(new User(userId));
            relationship.setFriend(new User(friendId));
            relationship.setStatus(Friend.Status.REQUEST);
            try {
                friendRepository.save(relationship);
                return new FriendDTO(userId, friendId, Friend.Status.REQUEST);
            } catch (DataIntegrityViolationException e) {
                log.error(e.getMessage());
                return null;
            }
        }
    }

    public List<FriendDTO> friendRequests(String userId, boolean isSend) {
        User user = new User(userId);

        List<Friend> friendEntities;
        if(isSend) {
            friendEntities = friendRepository.getFriendsByUserAndStatus(user, Friend.Status.REQUEST);
        } else {
            friendEntities = friendRepository.getFriendsByFriendAndStatus(user, Friend.Status.REQUEST);
        }

        return getFriendDTOS(friendEntities, isSend);
    }

    @Transactional
    public FriendDTO acceptFriend(String userId, String friendId) {
        // user friend
        // friend user
        // 둘 다 status.friend로 바꾸어야 한다
        Optional<Friend> friendToUser = friendRepository.findFriendByUserAndFriendAndStatus(new User(friendId), new User(userId), Friend.Status.REQUEST);

        if(friendToUser.isPresent()) {
            Optional<Friend> userToFriend = friendRepository.findFriendByUserAndFriend(new User(userId), new User(friendId));
            Friend ftu = friendToUser.get();

            Friend utf = userToFriend.orElseGet(() -> Friend.builder()
                    .user(new User(userId))
                    .friend(new User(friendId))
                    .build());

            ftu.setStatus(Friend.Status.FRIEND);
            utf.setStatus(Friend.Status.FRIEND);
            try {
                friendRepository.save(utf);
                friendRepository.save(ftu);
                return new FriendDTO(utf.getFriend().getId(), utf.getFriend().getNickname(), utf.getStatus());
            } catch (DataIntegrityViolationException e) {
                log.error(e.getMessage());
            }
        }
        return null;
    }

    @Transactional
    public UserNameDTO deleteFriendRequest(String userId, String targetId) {
        Optional<String> targetNickname = friendRepository.getNicknameByUserId(targetId);
        friendRepository.deleteFriendByUserAndFriendAndStatus(new User(targetId), new User(userId), Friend.Status.REQUEST);;

        return new UserNameDTO(targetId, targetNickname.get());
    }

    @Transactional
    public String deleteFriend(String userId, String friendId) {
        // 내 친구 삭제
        friendRepository.deleteFriendByUserAndFriendAndStatus(new User(userId), new User(friendId), Friend.Status.FRIEND);
        // 상대 친구 삭제
        friendRepository.deleteFriendByUserAndFriendAndStatus(new User(friendId), new User(userId), Friend.Status.FRIEND);
        return "Friendship deletion success";
    }

//    public List<RewardDTO> rewards(String userId) {
//
//    }
//
//    public RewardDTO buyReward(String userId, RewardDTO rewardDTO) {
//    }
//
//    public boolean updateProfileImage(String userId) {
//    }
//
//    public int points(String userId) {
//    }

    public String getNickname(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        return optionalUser.map(User::getNickname).orElse(null);
    }

    public User getUserById(String userId) throws Exception {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new Exception("User not found with id: " + userId);
        }
        return user;
    }
}

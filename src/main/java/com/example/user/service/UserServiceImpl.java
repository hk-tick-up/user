package com.example.user.service;

import com.example.user.dto.FriendDTO;
import com.example.user.dto.UserNameDTO;
import com.example.user.dto.UserSignupDTO;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public String signUp(UserSignupDTO user) {
        // 중복 가입 막아야
        if(userRepository.existsById(user.id()))
            return "Error: Duplicated User ID";
        if(userRepository.existsByNickname(user.nickname()))
            return "Error: Duplicated Nickname";

        User newUser = new User(
                user.id(),
                user.nickname(),
                passwordEncoder.encode(user.password()),
                user.age(),
                user.gender(),
                user.job(),
                0,
                null,
                null,
                new HashSet<>());
        newUser.getRoles().add(User.UserRole.ROLE_USER);

        User saved = userRepository.save(newUser);
        return "Info: Account Created at "+saved.getCreatedAt().toString();
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

    public String deleteAccountRequest(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {





        HttpSession session1 = request.getSession(false); // false로 하면 세션이 없을 경우 새로 생성하지 않음
        if (session1 == null) {
            log.warn("Session-before is invalid (logged out)");
        } else {
            log.warn("Session-before is still active, session ID: " + session1.getId());
        }




        String userId = authentication.getName();
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            // 로그인 된 계정 삭제예정으로 등록
            userRepository.updateDeleteRequestAt(userId, LocalDateTime.now());
        }
        else return "Error: User not found";

        // 로그인 중인 계정 로그아웃
        new Logout().logout(request, response, authentication);
        // 원본은 로그아웃 엔드포인트로 리다이렉트 된다. (코드 종료 후 security의 체인에 의해 그렇다는 모양)
//        new SecurityContextLogoutHandler().logout(request, response, authentication);


        HttpSession session = request.getSession(false); // false로 하면 세션이 없을 경우 새로 생성하지 않음
        if (session == null) {
            log.warn("Session-after is invalid (logged out)");
        } else {
            log.warn("Session-after is still active, session ID: " + session.getId());
        }


        return "Info: Successfully Deleted Account";
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
        else return null;
    }

    public User updatePersonalInfromation(String userId, Map<String, String> request) {
        User newUser = User.builder()
                .id(request.get("userId"))
                .nickname(request.get("nickname"))
                .age(Integer.parseInt(request.get("age")))
                .gender(User.Gender.valueOf(request.get("gender")))
                .job(request.get("job"))
                .build();
        // 수정 불가능한 값
        // gender, job -> 수정 X로 결정?
        // password, point, createdAt, deleteRequestAt, roles
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User oldUser = optionalUser.get();
            newUser.setPassword(oldUser.getPassword());
            return userRepository.save(newUser);
        }
        else return null;
    }

    public List<FriendDTO> friends(String userId) {
        User user = new User(userId);
        List<Friend> friendEntities = friendRepository.getFriendsByUserAndStatus(user, Friend.Status.FRIEND);
        return getFriendDTOS(friendEntities);
    }

    private List<FriendDTO> getFriendDTOS(List<Friend> friendEntities) {
        List<FriendDTO> friendDTOs = new ArrayList<>();
        for(Friend friendEntity : friendEntities) {
            User friend = friendEntity.getFriend();
            friendDTOs.add(
                    new FriendDTO(friend.getId(), friend.getNickname(), friendEntity.getStatus())
            );
        }
        return friendDTOs;
    }

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
            friendRepository.save(relationship);
            return new FriendDTO(userId, friendId, Friend.Status.REQUEST);
        }
    }

    public List<FriendDTO> friendRequests(String userId) {
        User user = new User(userId);
        List<Friend> friendEntities = friendRepository.getFriendsByUserAndStatus(user, Friend.Status.REQUEST);
        return getFriendDTOS(friendEntities);
    }

    public FriendDTO acceptFriend(String userId, String friendId) {
        // user friend
        // friend user
        // 둘 다 status.friend로 바꾸어야 한다
        Optional<Friend> userToFriend = friendRepository.findFriendByUserAndFriend(new User(userId), new User(friendId));
        Optional<Friend> friendToUser = friendRepository.findFriendByUserAndFriend(new User(friendId), new User(userId));

        if(userToFriend.isPresent() && friendToUser.isPresent()) {
            Friend utf = userToFriend.get();
            Friend ftu = friendToUser.get();
            utf.setStatus(Friend.Status.FRIEND);
            ftu.setStatus(Friend.Status.FRIEND);
            friendRepository.save(utf);
            friendRepository.save(ftu);
            return new FriendDTO(utf.getFriend().getId(), utf.getFriend().getNickname(), utf.getStatus());
        }
        else return null;
    }

    public UserNameDTO deleteFriendRequest(String userId, Long requestId) {
        Optional<Friend> friendOptional = friendRepository.deleteFriendByIdAndStatus(requestId, Friend.Status.REQUEST);
        if(friendOptional.isPresent()) {
            Friend friend = friendOptional.get();
            if(friend.getUser().getId().equals(userId)) {
                return new UserNameDTO(friend.getFriend().getId(), friend.getFriend().getNickname());
            }
        }
        return null;
    }

    public UserNameDTO deleteFriend(String userId, String friendId) {
        // 내 친구 삭제
        friendRepository.deleteFriendByUserAndFriendAndStatus(new User(userId), new User(friendId), Friend.Status.FRIEND);
        // 상대 친구 삭제
        friendRepository.deleteFriendByUserAndFriendAndStatus(new User(friendId), new User(userId), Friend.Status.FRIEND);
        return new UserNameDTO(userId, friendId);
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
}

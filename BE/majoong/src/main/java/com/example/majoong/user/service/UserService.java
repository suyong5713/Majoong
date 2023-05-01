package com.example.majoong.user.service;

import com.example.majoong.exception.*;
import com.example.majoong.tools.JwtTool;
import com.example.majoong.user.domain.User;
import com.example.majoong.user.dto.*;
import com.example.majoong.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private final RedisTemplate redisTemplate;

    private final JwtTool jwtTool;

    public UserInformationDto getUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split(" ")[1];
        int userId = jwtTool.getUserIdFromToken(token);

        User user = userRepository.findById(userId).get();
        if (user == null){
            throw new NoUserException();
        }
        UserInformationDto userInfo = new UserInformationDto();
        userInfo.setUserId(user.getId());
        userInfo.setPhoneNumber(user.getPhoneNumber());
        userInfo.setNickname(user.getNickname());
        userInfo.setProfileImage(user.getProfileImage());

        int count = redisTemplate.keys("notification:" + user.getId() + "_" + "*").size();
        userInfo.setAlarmCount(count);

        return userInfo;
    }
    public void signupUser(CreateUserDto createUserDto) {

        String phoneNumber = createUserDto.getPhoneNumber();
        String nickname = createUserDto.getNickname();
        String profileImage = createUserDto.getProfileImage();
        String pinNumber = createUserDto.getPinNumber();
        String socialPK = createUserDto.getSocialPK();


        User existingUser = userRepository.findByPhoneNumber(phoneNumber);
        if (existingUser != null) {
            throw new DuplicatePhoneNumberException();
        }

        User existingUser2 = userRepository.findBySocialPK(socialPK);
        if (existingUser2 != null) {
            throw new DuplicateSocialPKException();
        }

        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setNickname(nickname);
        user.setProfileImage(profileImage);
        user.setPinNumber(pinNumber);
        user.setSocialPK(socialPK);

        userRepository.save(user);
    }

    public TokenDto generateUser(int id) {
        String accessToken = jwtTool.createAccessToken(id);
        String refreshToken = jwtTool.createRefreshToken(id);
        TokenDto user = new TokenDto(id,accessToken,refreshToken);
        return user;
    }

    public UserResponseDto login(String socialPK){
        User findUser = userRepository.findBySocialPK(socialPK);
        if (findUser == null){
            throw new NoUserException();
        }
        if (findUser.getState() == 0) {
            throw new DeletedUserException();
        }
        TokenDto token = generateUser(findUser.getId());
        UserResponseDto user = new UserResponseDto();
        user.setUserId(findUser.getId());
        user.setAccessToken(token.getAccessToken());
        user.setRefreshToken(token.getRefreshToken());
        user.setPhoneNumber(findUser.getPhoneNumber());
        user.setPinNumber(findUser.getPinNumber());
        return user;
    }

    public UserResponseDto autoLogin(HttpServletRequest request){
        String token = request.getHeader("Authorization").split(" ")[1];
        int userId = jwtTool.getUserIdFromToken(token);
        User user = userRepository.findById(userId).get();
        return login(user.getSocialPK());
    }

    public TokenDto reToken(HttpServletRequest request){
        String token = request.getHeader("Authorization").split(" ")[1];
        if(token == null
                || !jwtTool.validateToken(token)) {
            throw new RefreshTokenException();
        }
        int userId = jwtTool.getUserIdFromToken(token);

        String newAccessToken = "Bearer " + jwtTool.createAccessToken(userId);
        TokenDto newToken = new TokenDto(userId, newAccessToken,token);
        return newToken;
    }

    public String withdrawal(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split(" ")[1];
        int userId = jwtTool.getUserIdFromToken(token);

        Optional<User> user = userRepository.findById(userId);
        if (user == null) {
            throw new NoUserException();
        }

        user.get().setState(0);
        userRepository.save(user.get());

        return "회원탈퇴 성공";
    }



}

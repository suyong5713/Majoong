package com.example.majoong.user.controller;

import com.example.majoong.response.ResponseData;
import com.example.majoong.user.dto.*;
import com.example.majoong.user.service.MessageService;
import com.example.majoong.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletRequest;


@RequestMapping("/user")
@RequiredArgsConstructor
@RestController
@Slf4j
public class UserController {
    private final UserService userService;
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity getUser(HttpServletRequest request) {
        ResponseData data = new ResponseData();
        data.setData(userService.getUser(request));
        data.setMessage("회원정보 조회 성공");
        return data.builder();
    }

    @PostMapping("/signup")
    public ResponseEntity joinUser(@RequestBody CreateUserDto user){
        ResponseData data = new ResponseData();
        userService.signupUser(user);
        data.setStatus(200);
        data.setMessage("회원가입 성공");
        return data.builder();
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDto info) {
        ResponseUserDto user = userService.login(info.getSocialPK());
        ResponseData data = new ResponseData();
        data.setData(user);
        data.setMessage("로그인 성공");
        return data.builder();
    }

    @PostMapping("/auto-login")
    public ResponseEntity autoLogin(HttpServletRequest request) {
        ResponseUserDto user = userService.autoLogin(request);
        ResponseData data = new ResponseData();
        data.setData(user);
        data.setMessage("자동 로그인");
        return data.builder();
    }
    @PostMapping("/withdrawal")
    public ResponseEntity withdrawal(HttpServletRequest request) {
        ResponseData data = new ResponseData();
        data.setMessage(userService.withdrawal(request));
        return data.builder();
    }

    @PostMapping("/retoken")
    public ResponseEntity reToken(HttpServletRequest request) {
        TokenDto newToken = userService.reToken(request);
        ResponseData data = new ResponseData();
        data.setData(newToken);
        data.setMessage("AccessToken 재발행 성공");
        return data.builder();
    }


    @PostMapping("/phone")
    public ResponseEntity<?> sendAuthNumber(@RequestBody PhoneNumberDto info) throws NoSuchAlgorithmException, URISyntaxException, InvalidKeyException, JsonProcessingException, UnsupportedEncodingException {
        ResponseData data = new ResponseData();
        data.setData(messageService.sendMessage(info.getPhoneNumber()));
        return data.builder();
    }

    @PostMapping("/phone/verify")
    public ResponseEntity<?> verifyAuthNumber(@RequestBody VerificationNumberDto info) {
        ResponseData data = new ResponseData();
        if (messageService.verifyNumber(info)){
            data.setMessage("인증 완료");
        }
        return data.builder();
    }

    @PutMapping("/pin")
    @Operation(summary = "pinNumber 수정", description = "pinNumber 수정 API 입니다.")
    public ResponseEntity<?> changePin(HttpServletRequest request, @RequestBody pinNumberDto info) {
        ResponseData data = new ResponseData();
        data.setData(userService.changePin(request, info.getPinNumber()));
        data.setMessage("pin 수정 성공");

        return data.builder();
    }

    @PutMapping("/profile")
    public ResponseEntity<?> changeProfile(HttpServletRequest request,
                                           @RequestPart("nickname") String nickname,
                                           @RequestPart("phoneNumber") String phoneNumber,
                                           @RequestPart("profileImage") MultipartFile profileImage) throws IOException {
        ResponseData data = new ResponseData();
        UserProfileRequestrDto userProfileRequestrDto = new UserProfileRequestrDto();
        userProfileRequestrDto.setPhoneNumber(phoneNumber);
        userProfileRequestrDto.setNickname(nickname);

        data.setData(userService.changeProfile(request, userProfileRequestrDto, profileImage));
        data.setMessage("회원정보 수정 성공");

        return data.builder();
    }
}

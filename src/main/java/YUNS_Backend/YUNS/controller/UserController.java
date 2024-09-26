package YUNS_Backend.YUNS.controller;

import YUNS_Backend.YUNS.dto.UserRegisterDto;
import YUNS_Backend.YUNS.entity.Role;
import YUNS_Backend.YUNS.entity.User;
import YUNS_Backend.YUNS.repository.UserRepository;
import YUNS_Backend.YUNS.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @PostMapping(value = "/api/register")
    public ResponseEntity<Object> register(@Valid @RequestBody UserRegisterDto userRegisterDto, BindingResult bindingResult){

        //입력된 값에 이상이 있을 경우 400에러 반환
        if(bindingResult.hasErrors()){
            Map<String, String> error = new HashMap<>();
            error.put("message", "잘못된 입력값이 있습니다.");

            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        try{
            User user = User.createUser(userRegisterDto, passwordEncoder);
            userService.registerUser(user);
        }catch (IllegalStateException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){ //서비스에서 에러 발생시 500에러 반환
            Map<String, String> error = new HashMap<>();
            error.put("message", "회원가입에 실패했습니다");

            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "회원가입이 완료되었습니다");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/api/users/{userId}")
    public @ResponseBody ResponseEntity unRegister(@PathVariable("userId") Long userId, Principal principal){
        String studentNumber = principal.getName();
        User user = userService.findUserByStudentNumber(studentNumber);

        if(userId != user.getUserId()){
            Map<String, String> error = new HashMap<>();
            error.put("message", "해당 요청에 대한 권한이 없습니다");

            return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        try{
            userService.deleteUser(userId);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "회원탈퇴가 완료되었습니다");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/createAdmin")
    public @ResponseBody ResponseEntity createAdmin(@Valid @RequestBody UserRegisterDto userRegisterDto, BindingResult bindingResult){
        //입력된 값에 이상이 있을 경우 400에러 반환
        if(bindingResult.hasErrors()){
            Map<String, String> error = new HashMap<>();
            error.put("message", "잘못된 입력값이 있습니다");

            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .studentNumber(userRegisterDto.getStudentNumber())
                .name(userRegisterDto.getName())
                .password(passwordEncoder.encode(userRegisterDto.getPassword()))
                .phoneNumber(userRegisterDto.getPhoneNumber())
                .email(userRegisterDto.getEmail())
                .role(Role.ADMIN)
                .userRentalStatus(false)
                .build();

        userRepository.save(user);


        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/api/admin/userlist")
    public ResponseEntity<Object> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userService.getAllUsers(pageable);

            return new ResponseEntity<>(userPage, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("전체 사용자 정보 리스트 조회에 실패하였습니다");
        }
    }


    @GetMapping("/api/admin/userlist/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable("userId") Long userId) {
        try {
            Optional<User> user = userService.findUserById(userId);
            if (user.isPresent()) {
                return new ResponseEntity<>(user.get(), HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("해당 유저를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("유저 조회에 실패하였습니다.");
        }
    }
}

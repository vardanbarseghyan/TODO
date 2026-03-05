//package com.vardan.todo.controller;
//
//import com.vardan.todo.dto.request.LoginRequest;
//import com.vardan.todo.dto.response.ApiResponse;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("api/v1/auth")
//public class AuthController {
//    @PostMapping("/register")
//    public ResponseEntity<?> register()
//    {
//        //Register new user
//    }
//    @PostMapping("/login")
//    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest)
//    {
//        //Login with email/password
//    }
//    @PostMapping("/refresh")
//    public void refreshNewAccessToken()
//    {
//        //Get new access token
//    }
//    @PostMapping("/logout")
//    public ResponseEntity<?> logout()
//    {
//        //Logout, revoke tokens
//    }
//}

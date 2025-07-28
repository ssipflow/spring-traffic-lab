package com.ssipflow.backend.controller;

import com.ssipflow.backend.dto.SignUpUser;
import com.ssipflow.backend.dto.UserLogin;
import com.ssipflow.backend.entity.User;
import com.ssipflow.backend.jwt.JwtUtil;
import com.ssipflow.backend.service.CustomUserDetailService;
import com.ssipflow.backend.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Value("${variables.jwt.expiration}")
    private long expirationTime;

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailService userDetailService;

    @GetMapping("")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @PostMapping("/signUp")
    public ResponseEntity<User> createUser(@RequestBody SignUpUser signUpUser) {
        User user = userService.createUser(signUpUser);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to be deleted", required = true)
            @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public String login(@RequestBody UserLogin userLogin,
                        HttpServletResponse response) throws AuthenticationException {
        // authenticate with username, password
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword()));

        // loadUserByUsername for JWT payload information
        UserDetails userDetails = userDetailService.loadUserByUsername(userLogin.getUsername());

        String token = jwtUtil.generateToken(userDetails.getUsername());
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) expirationTime);

        response.addCookie(cookie);
        return token;
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        // Remove browser cookie token
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

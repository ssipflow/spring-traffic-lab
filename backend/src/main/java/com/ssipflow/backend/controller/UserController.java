package com.ssipflow.backend.controller;

import com.ssipflow.backend.dto.SignUpUser;
import com.ssipflow.backend.dto.UserLogin;
import com.ssipflow.backend.entity.User;
import com.ssipflow.backend.jwt.JwtUtil;
import com.ssipflow.backend.service.CustomUserDetailService;
import com.ssipflow.backend.service.JwtBlacklistService;
import com.ssipflow.backend.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.naming.AuthenticationException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
    private final JwtBlacklistService jwtBlacklistService;

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
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLogin.getUsername(),
                userLogin.getPassword()));

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

    @PostMapping("/logout/all")
    public void logoutAll(
            @RequestParam(required = false) String requestToken,
            @CookieValue(value = "token", required = false) String cookieToken,
            HttpServletRequest request, HttpServletResponse response) {
        String token = null;
        String bearerToken = request.getHeader("Authorization");
        if (requestToken != null) {
            token = requestToken;
        } else if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        } else if (cookieToken != null) {
            token = cookieToken;
        }
        Instant now = new Date().toInstant();
        LocalDateTime expirationTime = now.atZone(ZoneId.systemDefault()).toLocalDateTime();

        String username = jwtUtil.getUsername(token);
        jwtBlacklistService.blacklistToken(token, expirationTime, username);

        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);    // Delete cookie
        response.addCookie(cookie);
    }

    @PostMapping("token/validation")
    @ResponseStatus(HttpStatus.OK)
    public void jwtValidate(@RequestParam String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid JWT");
        }
    }
}

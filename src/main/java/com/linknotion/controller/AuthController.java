package com.linknotion.controller;

import com.linknotion.entity.User;
import com.linknotion.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String register(@RequestParam String username, @RequestParam String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/signup?error";
        }
        userRepository.save(new User(username, passwordEncoder.encode(password)));
        return "redirect:/login";
    }
}

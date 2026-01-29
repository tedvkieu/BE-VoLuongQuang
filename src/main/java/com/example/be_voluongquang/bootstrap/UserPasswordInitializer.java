package com.example.be_voluongquang.bootstrap;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.be_voluongquang.entity.UserEntity;
import com.example.be_voluongquang.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPasswordInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        List<UserEntity> users = userRepository.findAll();
        int updated = 0;
        for (UserEntity u : users) {
            String pw = u.getPassword();
            if (pw == null || !pw.startsWith("$2")) { // not bcrypt
                String raw = (pw == null || pw.isBlank()) ? "123456" : pw;
                String hashed = passwordEncoder.encode(raw);
                u.setPassword(hashed);
                updated++;
            }
        }
        if (updated > 0) {
            userRepository.saveAll(users);
           
        } else {
            log.info("All user passwords already hashed. No action taken.");
        }
    }
}


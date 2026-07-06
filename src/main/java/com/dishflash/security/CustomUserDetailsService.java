package com.dishflash.security;

import com.dishflash.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phoneBlindIndex) throws UsernameNotFoundException {
        com.dishflash.entity.User user = userRepository.findByPhoneBlindIndex(phoneBlindIndex)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(user.getId().toString(), user.getPasswordHash(), Collections.emptyList());
    }
}

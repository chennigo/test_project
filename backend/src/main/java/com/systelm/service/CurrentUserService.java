package com.systelm.service;

import com.systelm.entity.UserAccount;
import com.systelm.repository.UserAccountRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserAccountRepository userAccountRepository;

    public CurrentUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount requireCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("当前用户不存在"));
    }
}

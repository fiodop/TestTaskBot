package com.testtaskbot.service;

import com.testtaskbot.model.entity.AppUser;
import com.testtaskbot.repository.AppUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppUserService {
    private AppUserRepository appUserRepository;

    public void save(AppUser appUser) {
        appUserRepository.save(appUser);
    }
}

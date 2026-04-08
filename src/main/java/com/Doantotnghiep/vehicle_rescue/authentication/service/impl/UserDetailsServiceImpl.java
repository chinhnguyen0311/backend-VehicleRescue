package com.Doantotnghiep.vehicle_rescue.authentication.service.impl;

import com.Doantotnghiep.vehicle_rescue.authentication.service.AccountService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AccountService accountService;

    public UserDetailsServiceImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.Doantotnghiep.vehicle_rescue.authentication.entity.Account account = this.accountService.getAccountByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        return new User(
                account.getUsername(),
                account.getPassword(),
                Collections.emptyList()
        );
    }
}


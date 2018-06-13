package com.doitincloud.oauth2.services;

import com.doitincloud.oauth2.models.User;
import com.doitincloud.oauth2.supports.OAuthUtils;
import com.doitincloud.oauth2.repositories.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {

    private UserRepo userRepo;

    public UserDetailsServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username + " not found");
        }
        UserDetails userDetails = OAuthUtils.toUserDetails(user);
        //System.out.println("UserDetailsService -> user:\n"+Utils.toPrettyJson(userDetails));
        return userDetails;
    }
}

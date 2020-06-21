package com.sili.service;

import com.sili.model.UserTO;
import com.sili.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public Uni<UserTO> authenticateUser(String username) {
        return userRepository.getUserByUsername(username);
    }
}

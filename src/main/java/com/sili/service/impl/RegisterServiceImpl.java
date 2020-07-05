package com.sili.service.impl;

import com.sili.exceptions.UserAlreadyExistException;
import com.sili.model.RegisterTO;
import com.sili.repository.UserRepository;
import com.sili.service.RegisterService;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;

    @Override
    public Uni<RegisterTO> registerUser(RegisterTO user) {
        return userRepository.getUserByUsername(user.getUsername())
            .onItem().ifNull().failWith(NullPointerException::new)
            .onItem().failWith(none -> new UserAlreadyExistException())
            .onFailure(NullPointerException.class)
            .recoverWithUni(userRepository.registerUser(user))
            .onFailure(UserAlreadyExistException.class)
            .recoverWithUni(Uni.createFrom().nullItem());
    }
}

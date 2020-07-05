package com.sili.service;

import com.sili.exceptions.UserAlreadyExistException;
import com.sili.model.RegisterTO;
import com.sili.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;

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

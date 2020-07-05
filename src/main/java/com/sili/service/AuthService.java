package com.sili.service;

import com.sili.exceptions.UserNotFoundException;
import com.sili.model.RegisterTO;
import com.sili.model.TokenTO;
import com.sili.model.UserTO;
import com.sili.repository.AuthStateRepository;
import com.sili.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import java.util.Random;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthStateRepository authStateRepository;

    public Uni<TokenTO> generateToken(UserTO user) {
        return userRepository.getUserIdByUsername(user.getUsername())
            .onItem().ifNull().failWith(UserNotFoundException::new)
            .onItem().produceUni(authStateRepository::checkAuthState)
            .onItem().ifNull().failWith(NullPointerException::new)
            .onFailure(NullPointerException.class)
            .recoverWithUni(userRepository.getUserIdByUsername(user.getUsername()))
            .onItem().apply(id -> getAuthStateInitValues(user.getUsername(), id))
            .onItem().produceUni(authStateRepository::createAuthState);
    }

    private Tuple3<UUID, Long, Long> getAuthStateInitValues(String username, Long userId) {
        return Tuple3.of(getToken(username), userId, (long) getReps());
    }

    private UUID getToken(String username) {
        // TODO
//        byte[] result;
//        try {
//            byte[] nameSpaceBytes = bytesFromUUID(namespace);
//            byte[] nameBytes = username.getBytes("UTF-8");
//            result = joinBytes(nameSpaceBytes, nameBytes);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        UUID uuid = UUID.nameUUIDFromBytes(result);
        return UUID.randomUUID();
    }

    private int getReps() {
        return new Random().nextInt(5) + 1;
    }

    public Uni<RegisterTO> authenticateUser(String username) {
        return userRepository.getUserByUsername(username);
    }
}

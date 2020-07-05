package com.sili.service.impl;

import com.sili.exceptions.StateNotFoundException;
import com.sili.exceptions.UnauthorizedException;
import com.sili.exceptions.UserNotFoundException;
import com.sili.model.AValueTO;
import com.sili.model.SessionResponseTO;
import com.sili.model.SessionTO;
import com.sili.model.TokenResponseTO;
import com.sili.model.UserTO;
import com.sili.model.XValueTO;
import com.sili.model.YValueTO;
import com.sili.repository.AuthStateRepository;
import com.sili.repository.SessionRepository;
import com.sili.repository.UserRepository;
import com.sili.service.AuthService;
import com.sili.service.utils.AuthUtils;
import io.smallrye.mutiny.Uni;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthStateRepository authStateRepository;
    private final SessionRepository sessionRepository;

    private final AuthUtils authUtils;

    @Override
    public Uni<TokenResponseTO> generateToken(UserTO user) {
        return userRepository.getUserIdByUsername(user.getUsername())
            .onItem().ifNull().failWith(UserNotFoundException::new)
            .onItem().produceUni(authStateRepository::checkAuthState)
            .onItem().ifNull().failWith(NullPointerException::new)
            .onFailure(NullPointerException.class)
            .recoverWithUni(userRepository.getUserIdByUsername(user.getUsername()))
            .onItem().apply(id -> authUtils.getAuthStateInitValues(user.getUsername(), id))
            .onItem().produceUni(authStateRepository::createAuthState);
    }

    @Override
    public Uni<AValueTO> generateAVector(XValueTO xValue) {
        return authStateRepository.getUserId(xValue.getToken())
            .onItem().ifNull().failWith(StateNotFoundException::new)
            .onItem().produceUni(userRepository::getKeyByUserId)
            .onItem().apply(key -> authUtils.generateAVector(key.size()))
            .onItem().produceUni(vector -> authStateRepository.updateValues(xValue.getToken(), xValue.getX(), vector));
    }

    @Override
    public Uni<SessionResponseTO> authenticate(YValueTO yValue) {
        // get public key from userRepository getKeyByUserId()
        Uni<AValueTO> yvalueUni = authStateRepository.getYValueForToken(yValue.getToken());

        return yvalueUni.onItem().ifNull().failWith(StateNotFoundException::new)
            .and().uni(
                yvalueUni.onItem().produceUni(val -> userRepository.getKeyByUserId(val.getUserId().longValue())))
            .asTuple()
            .onItem().apply(tuple -> authUtils.checkYValue(
                tuple.getItem2(),
                tuple.getItem1().getX(),
                yValue.getY(),
                tuple.getItem1().getA(),
                39769L * 50423
                )
            )
            .onItem().ifNull().failWith(UnauthorizedException::new)
            .onItem().produceUni(none -> authStateRepository.incrementSuccessTries(yValue.getToken()))
            .onItem().apply(authUtils::isAuthorized)
            .onItem().produceUni(session -> yvalueUni.onItem().produceUni(
                val -> handleCreatedSession(session, yValue.getToken(), val.getUserId().longValue())));
    }

    private Uni<SessionResponseTO> handleCreatedSession(SessionResponseTO session, UUID token, Long userId) {
        System.out.println(session.getIs_authenticated());
        if (session.getIs_authenticated()) {
            System.out.println("SAVE SESSION");
            return authStateRepository.removeToken(session, token)
                .onItem().produceUni(n -> sessionRepository.saveSession(SessionTO.of(token, userId, LocalDateTime.now())))
                .onItem().apply(s -> session);
        }

        return Uni.createFrom().item(session);
    }
}

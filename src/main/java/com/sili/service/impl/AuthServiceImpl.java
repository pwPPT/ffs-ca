package com.sili.service.impl;

import com.sili.exceptions.StateNotFoundException;
import com.sili.exceptions.UnauthorizedException;
import com.sili.exceptions.UserNotFoundException;
import com.sili.model.AValueTO;
import com.sili.model.SessionTO;
import com.sili.model.TokenTO;
import com.sili.model.UserTO;
import com.sili.model.XValueTO;
import com.sili.model.YValueTO;
import com.sili.repository.AuthStateRepository;
import com.sili.repository.UserRepository;
import com.sili.service.AuthService;
import com.sili.service.utils.AuthUtils;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthStateRepository authStateRepository;

    private final AuthUtils authUtils;

    @Override
    public Uni<TokenTO> generateToken(UserTO user) {
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
    public Uni<SessionTO> authenticate(YValueTO yValue) {
        // get public key from userRepository getKeyByUserId()
        return authStateRepository.getYValueForToken(yValue.getToken())
            .onItem().ifNull().failWith(StateNotFoundException::new)
            .and().uni(authStateRepository.getYValueForToken(yValue.getToken())
            .onItem().produceUni(val -> userRepository.getKeyByUserId(val.getUserId().longValue()))).asTuple()
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
            .onItem().apply(authUtils::isAuthorized);
    }
}

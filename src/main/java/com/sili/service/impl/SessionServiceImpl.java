package com.sili.service.impl;

import com.sili.exceptions.UnauthorizedException;
import com.sili.model.SecretTO;
import com.sili.model.SessionRequestTO;
import com.sili.repository.SessionRepository;
import com.sili.service.SessionService;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    private final SecretTO secret = new SecretTO("Super secret :)");

    @Override
    public Uni<SecretTO> getSecret(SessionRequestTO sessionRequest) {
        return sessionRepository.getSession(sessionRequest.getSession_id())
            .onItem().ifNull().failWith(UnauthorizedException::new)
            .onItem().apply(none -> secret);
    }
}

package com.sili.service;

import com.sili.model.SecretTO;
import com.sili.model.SessionRequestTO;
import io.smallrye.mutiny.Uni;

public interface SessionService {

    Uni<SecretTO> getSecret(SessionRequestTO sessionRequest);
}

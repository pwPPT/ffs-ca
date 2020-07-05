package com.sili.service;

import com.sili.model.AValueTO;
import com.sili.model.TokenTO;
import com.sili.model.UserTO;
import com.sili.model.XValueTO;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniEmitter;

public interface AuthService {

    Uni<TokenTO> generateToken(UserTO user);

    Uni<AValueTO> generateAVector(XValueTO xValue);
}

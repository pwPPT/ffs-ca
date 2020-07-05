package com.sili.service;

import com.sili.model.AValueTO;
import com.sili.model.SessionResponseTO;
import com.sili.model.TokenResponseTO;
import com.sili.model.UserTO;
import com.sili.model.XValueTO;
import com.sili.model.YValueTO;
import io.smallrye.mutiny.Uni;

public interface AuthService {

    Uni<TokenResponseTO> generateToken(UserTO user);

    Uni<AValueTO> generateAVector(XValueTO xValue);

    Uni<SessionResponseTO> authenticate(YValueTO yValue);
}

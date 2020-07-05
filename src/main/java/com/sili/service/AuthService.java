package com.sili.service;

import com.sili.model.AValueTO;
import com.sili.model.SessionTO;
import com.sili.model.TokenTO;
import com.sili.model.UserTO;
import com.sili.model.XValueTO;
import com.sili.model.YValueTO;
import io.smallrye.mutiny.Uni;

public interface AuthService {

    Uni<TokenTO> generateToken(UserTO user);

    Uni<AValueTO> generateAVector(XValueTO xValue);

    Uni<SessionTO> authenticate(YValueTO yValue);
}

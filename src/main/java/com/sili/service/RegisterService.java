package com.sili.service;

import com.sili.model.RegisterTO;
import io.smallrye.mutiny.Uni;

public interface RegisterService {

    Uni<RegisterTO> registerUser(RegisterTO user);
}

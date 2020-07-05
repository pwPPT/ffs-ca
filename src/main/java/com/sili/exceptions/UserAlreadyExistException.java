package com.sili.exceptions;

import com.sili.model.RegisterTO;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor
public class UserAlreadyExistException extends RuntimeException {

    public UserAlreadyExistException(RegisterTO user) {
    }
}

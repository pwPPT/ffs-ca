package com.sili.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenTO {

    UUID token;
    Integer reps;
    Integer succTries;
}

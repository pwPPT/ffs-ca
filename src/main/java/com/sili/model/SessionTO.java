package com.sili.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionTO {

    Boolean repeat;
    Boolean is_authenticated;
    String session_id;
}

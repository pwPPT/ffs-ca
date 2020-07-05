package com.sili.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionResponseTO {

    Boolean repeat;
    Boolean is_authenticated;
    String session_id;
}

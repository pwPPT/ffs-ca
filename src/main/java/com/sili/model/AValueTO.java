package com.sili.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AValueTO {

    Long x;
    List<Integer> a;
    Integer userId;
}

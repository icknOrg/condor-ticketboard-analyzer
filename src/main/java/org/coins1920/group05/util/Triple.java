package org.coins1920.group05.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Triple<U, V, W> {

    private U first;
    private V second;
    private W third;
}

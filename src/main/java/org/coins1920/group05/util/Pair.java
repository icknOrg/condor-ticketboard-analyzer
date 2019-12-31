package org.coins1920.group05.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Pair<U, V> implements Serializable {

    private U first;
    private V second;
}

package org.coins1920.group05.fetcher.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<U, V> {

    private U first;
    private V second;

}

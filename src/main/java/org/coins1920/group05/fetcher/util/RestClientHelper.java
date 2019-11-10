package org.coins1920.group05.fetcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RestClientHelper {
    private static Logger logger = LoggerFactory.getLogger(RestClientHelper.class);

    public static <T> List<T> nonNullResponseEntities(ResponseEntity<T[]> response) {
        if (response.getBody() == null) {
            return new LinkedList<>();
        } else {
            logger.info("I got " + response.getBody().length + " item(s)!");
            return Arrays
                    .stream(response.getBody())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }
}

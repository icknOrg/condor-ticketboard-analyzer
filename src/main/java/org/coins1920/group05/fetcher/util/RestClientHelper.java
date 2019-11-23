package org.coins1920.group05.fetcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RestClientHelper {
    private static Logger logger = LoggerFactory.getLogger(RestClientHelper.class);

    public static <T> List<T> nonNullResponseEntities(ResponseEntity<T[]> response) {
        if (response == null || response.getBody() == null) {
            return new LinkedList<>();
        } else {
            logger.info("I got " + response.getBody().length + " item(s)!");
            return Arrays
                    .stream(response.getBody())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    public static Optional<String> splitGithubPaginationLinks(String links) {
        final String regExp = "[<][^,]+[;].rel[^,]+";
        final Matcher matcher = Pattern
                .compile(regExp)
                .matcher(links);

        // TODO: readme! Since Java 9 there's a cool function: Matcher.results()
        // It makes the following super-ugly while loop unnecessary:
        final LinkedList<String> matches = new LinkedList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        // we chose Java 8 for compatibility reasons, but one should rather use Java 9+ ASAP!

        return matches
                .stream()
                .filter(s -> s.contains("rel=\"next\""))
                .map(l -> l.split(";")[0])
                .map(String::trim)
                .map(l -> l.substring(1, (l.length() - 1)))
                .findFirst();
    }
}

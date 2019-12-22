package org.coins1920.group05.fetcher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URI;
import java.util.List;

/**
 * Holds the result of a set of queries: all entities that could successfully
 * be fetched, a flag, whether the GitHub rate limit exceeded and two list of
 * all visited URLs (those that worked and those that failed).
 *
 * @param <T> the type of the entities fetched
 */
@Data
@AllArgsConstructor
public class FetchingResult<T> {

    // all entities that could be fetched:
    private List<T> entities;

    // did a 401 occur (i.e. rate limit exceeded)?
    private boolean rateLimitOccurred;

    // all URLs that could be fetched:
    private List<URI> visitedUrls;

    // all URLs where a 401 occurred:
    private List<URI> failedUrls;
}

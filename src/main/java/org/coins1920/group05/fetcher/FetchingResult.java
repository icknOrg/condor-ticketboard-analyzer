package org.coins1920.group05.fetcher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Holds the result of a set of queries: all entities that could successfully
 * be fetched, a flag, whether the GitHub rate limit exceeded and two list of
 * all visited URLs (those that worked and those that failed).
 *
 * @param <T> the type of the entities fetched
 * @author Patrick Preuß (patrickp89)
 * @author Julian Cornea (buggitheclown)
 */
@Data
@AllArgsConstructor
public class FetchingResult<T> implements Serializable {

    // all entities that could be fetched:
    private List<T> entities;

    // did a 401 occur (i.e. rate limit exceeded)?
    private boolean rateLimitOccurred;

    // all URLs that could be fetched:
    private List<String> visitedUrls;

    // all URLs where a 401 occurred:
    private List<String> failedUrls;

    public FetchingResult() {
        this.entities = new LinkedList<>();
        this.rateLimitOccurred = false;
        this.visitedUrls = new LinkedList<>();
        this.failedUrls = new LinkedList<>();
    }

    public FetchingResult(List<T> entities) {
        this.entities = entities;
        this.rateLimitOccurred = false;
        this.visitedUrls = new LinkedList<>();
        this.failedUrls = new LinkedList<>();
    }

    /**
     * Combines to given FetchingResults into a single one.
     *
     * @param fetchingResult1 the first FetchingResult
     * @param fetchingResult2 the second FetchingResult
     * @param <U>             type parameter
     * @return the union FetchingResult
     */
    public static <U> FetchingResult<U> union(FetchingResult<U> fetchingResult1, FetchingResult<U> fetchingResult2) {
        final String m = "The argument must not be null!";
        if (fetchingResult1 == null && fetchingResult2 == null) {
            return new FetchingResult<>();
        }
        if (fetchingResult1 == null) {
            return Objects.requireNonNull(fetchingResult2, m);
        }
        if (fetchingResult2 == null) {
            return Objects.requireNonNull(fetchingResult1, m);
        }

        final List<U> combinedEntities = io.vavr.collection.List
                .ofAll(fetchingResult1.getEntities())
                .appendAll(fetchingResult2.getEntities())
                .toJavaList();

        boolean combinedRateLimitOccurred =
                fetchingResult1.isRateLimitOccurred() || fetchingResult2.isRateLimitOccurred();

        final List<String> combinedVisitedUrls = io.vavr.collection.List
                .ofAll(fetchingResult1.getVisitedUrls())
                .appendAll(fetchingResult2.getVisitedUrls())
                .toJavaList();

        final List<String> combinedFailedUrls = io.vavr.collection.List
                .ofAll(fetchingResult1.getFailedUrls())
                .appendAll(fetchingResult2.getFailedUrls())
                .toJavaList();

        return new FetchingResult<U>(
                combinedEntities,
                combinedRateLimitOccurred,
                combinedVisitedUrls,
                combinedFailedUrls
        );
    }
}

package org.coins1920.group05.condorizor;

import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.coins1920.group05.fetcher.FetchingResult;
import org.coins1920.group05.fetcher.GitHubIssueFetcher;
import org.coins1920.group05.fetcher.PartialFetchingResult;
import org.coins1920.group05.model.condor.Actor;
import org.coins1920.group05.model.condor.Edge;
import org.coins1920.group05.model.condor.EdgeType;
import org.coins1920.group05.model.general.Interaction;
import org.coins1920.group05.model.github.rest.Comment;
import org.coins1920.group05.model.github.rest.Issue;
import org.coins1920.group05.model.github.rest.User;
import org.coins1920.group05.util.Pair;
import org.coins1920.group05.util.PersistenceHelper;
import org.coins1920.group05.util.TimeFormattingHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Does the majority of the heavy lifting regarding GitHub fetching logic.
 *
 * @author Patrick Preuß (patrickp89)
 * @author Julian Cornea (buggitheclown)
 */
@Slf4j
public class GitHubRepoCondorizor {

    private final GitHubIssueFetcher fetcher;

    public GitHubRepoCondorizor(boolean paginate) {
        final String oauthToken = System.getenv("GITHUB_OAUTH_KEY");
        this.fetcher = new GitHubIssueFetcher(oauthToken, paginate);
    }

    /**
     * Fetches all issues (tickets) from the given GitHub repository. Returns either
     * a serialized partial result or a pair of Condor (Actor-, Edges-) files.
     * The partial result can be used to continue fetching at a later point in time.
     *
     * @param owner              repo owner
     * @param board              repo name
     * @param fetchClosedTickets whether to fetch closed tickets as well or not
     * @param outputDir          folder where results should be persisted
     * @return either a serialized partial result or a pair of Condor (Actor-, Edges-) files
     * @throws IOException if persisting to one of the files didn't work
     */
    public Either<File, Pair<File, File>> fetchGitHubIssues(
            String owner, String board, boolean fetchClosedTickets, String outputDir) throws IOException, ClassNotFoundException {
        // check if there already exists a partial result in the given output directory:
        if (PersistenceHelper.checkForPartialResult(owner, board, outputDir)) {
            final PartialFetchingResult<Issue, User, Comment> partialFetchingResult = PersistenceHelper
                    .readPersistedPartialResult(owner, board, outputDir);
            log.debug("There are partial results!");
            final FetchingResult<Issue> issueFetchingResult = partialFetchingResult.getIssueFetchingResult();
            final List<Pair<Issue, FetchingResult<Comment>>> commentsFetchingResults =
                    partialFetchingResult.getCommentsFetchingResults();
            return fetchEverything(owner, board, fetchClosedTickets, outputDir, issueFetchingResult, commentsFetchingResults);

        } else {
            return fetchEverything(owner, board, fetchClosedTickets, outputDir, null, null);
        }
    }

    private Either<File, Pair<File, File>> fetchEverything(
            String owner, String board, boolean fetchClosedTickets, String outputDir,
            FetchingResult<Issue> formerIssueFetchingResult,
            List<Pair<Issue, FetchingResult<Comment>>> formerCommentsFetchingResults) throws IOException {
        // first, fetch all issues of the given repo:
        final FetchingResult<Issue> issueFetchingResult = fetcher
                .fetchTickets(owner, board, fetchClosedTickets,
                        (formerIssueFetchingResult != null && formerIssueFetchingResult.getVisitedUrls() != null)
                                ? formerIssueFetchingResult.getVisitedUrls() : new LinkedList<>());

        // did we run into a rate limit?
        final boolean rateLimitOccurredForIssues = issueFetchingResult.isRateLimitOccurred();
        if (rateLimitOccurredForIssues) {
            log.warn("A rate limit occurred when fetching issues!");
        }

        // no, then let's unwrap the issues from the FetchingResult object:
        final List<Issue> githubIssues = issueFetchingResult.getEntities();

        // compute all visited comment-URLs into a single list:
        final List<String> visitedCommentUrls = (formerCommentsFetchingResults != null)
                ? formerCommentsFetchingResults
                .stream()
                .map(cfr -> cfr
                        .getSecond()
                        .getVisitedUrls())
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                : new LinkedList<>();

        // fetch all comments and the corresponding users for all issues:
        final List<Pair<Issue, FetchingResult<Comment>>> commentsForTicketResults = githubIssues
                .parallelStream()
                .map(i -> new Pair<>(i, fetcher.fetchCommentsForTicket(i, visitedCommentUrls)))
                .collect(Collectors.toList());

        // did we run into a rate limit?
        final boolean rateLimitOccurredForComments = commentsForTicketResults
                .stream()
                .anyMatch(c -> c.getSecond().isRateLimitOccurred());
        if (rateLimitOccurredForComments) {
            log.warn("A rate limit occurred when fetching comments!");
        }

        // no, then let's unwrap the comments from the FetchingResult objects:
        final List<Pair<Issue, List<Comment>>> comments = commentsForTicketResults
                .stream()
                .map(c -> new Pair<>(c.getFirst(), c.getSecond().getEntities()))
                .collect(Collectors.toList());

        // TODO : fetch all assignees for all tickets:
//        final List<Pair<Issue, List<User>>> assignees = githubIssues
//                .parallelStream()
//                .map(i -> new Pair<>(i, fetcher.fetchAssigneesForTicket(i)))
//                .collect(Collectors.toList());


        // re-try fetching the failed issue URLs:
        final BiFunction<String, List<String>, FetchingResult<Issue>> issueFetcherFunction
                = (u, visitedUrlsList) -> fetcher.retryTicketFetching(u, owner, board, visitedUrlsList);
        final FetchingResult<Issue> retriedIssuesFetchingResult = retryIssueFetching(
                issueFetcherFunction, formerIssueFetchingResult, issueFetchingResult);

        // ...and the failed comment URLs:
        final BiFunction<String, List<String>, FetchingResult<Comment>> commentFetcherFunction
                = (u, visitedUrlsList) -> fetcher.retryCommentFetching(u, owner, board, visitedUrlsList);
        final List<Pair<Issue, FetchingResult<Comment>>> retriedCommentsFetchingResult = retryCommentsFetching(
                commentFetcherFunction, formerCommentsFetchingResults, commentsForTicketResults);

        // combine everything! first, the issues:
        final FetchingResult<Issue> combinedIssueFetchingResult = CondorizorUtils
                .combineIssueFetchingResults(
                        formerIssueFetchingResult,
                        issueFetchingResult,
                        retriedIssuesFetchingResult
                );

        // then the comments:
        final List<Pair<Issue, FetchingResult<Comment>>> combinedCommentsFetchingResult = CondorizorUtils
                .combineCommentsFetchingResults(
                        CondorizorUtils.combineCommentsFetchingResults(
                                formerCommentsFetchingResults, commentsForTicketResults),
                        retriedCommentsFetchingResult);

        if (rateLimitOccurredForIssues || rateLimitOccurredForComments) {
            // a rate limit occurred, persist the partial result to disc:
            final PartialFetchingResult<Issue, User, Comment> partialFetchingResult
                    = new PartialFetchingResult<>(combinedIssueFetchingResult, combinedCommentsFetchingResult);
            return Either.left(PersistenceHelper.persistPartialResultsToDisk(partialFetchingResult, owner, board, outputDir));
        } else {
            // combine everything:
            final List<Issue> allIssues = io.vavr.collection.List
                    .ofAll(githubIssues)
                    .appendAll(combinedIssueFetchingResult.getEntities())
                    .toJavaList();

            final List<Pair<Issue, List<Comment>>> newComments = combinedCommentsFetchingResult
                    .stream()
                    .map(p -> new Pair<>(p.getFirst(), p.getSecond().getEntities()))
                    .collect(Collectors.toList());

            final List<Pair<Issue, List<Comment>>> allComments = io.vavr.collection.List
                    .ofAll(comments)
                    .appendAll(CondorizorUtils.combineIssueCommentsPairs(comments, newComments))
                    .toJavaList();

            // aggregate all users, map to Condor Actors/Edges and write to CSV files:
            return Either.right(condorizeIssuesAndUsers(allIssues, allComments, outputDir
            ));
        }
    }

    /**
     * Re-tries to fetch failed Issue URLs from the run(s) before this one. Ensures that no URLs
     * are visited that we _have_ seen during this run.
     *
     * @param fetcherFunction      a function that does the actual fetching
     * @param formerFetchingResult the result from the former run
     * @param newFetchingResult    the results we've seen so far during this run
     * @return the new results
     */
    private FetchingResult<Issue> retryIssueFetching(
            BiFunction<String, List<String>, FetchingResult<Issue>> fetcherFunction,
            FetchingResult<Issue> formerFetchingResult, FetchingResult<Issue> newFetchingResult) {
        // are there any former results at all?
        if (formerFetchingResult != null && formerFetchingResult.getFailedUrls() != null) {
            // combine the old and newly visited URLs:
            final List<String> visitedIssueUrls = io.vavr.collection.List
                    .ofAll((formerFetchingResult.getVisitedUrls() != null)
                            ? formerFetchingResult.getVisitedUrls() : new LinkedList<>())
                    .appendAll(newFetchingResult.getVisitedUrls())
                    .toJavaList();

            // retry fetching all failed issue URLs:
            log.debug("Trying to re-fetch formerly failed URLs...");
            return formerFetchingResult
                    .getFailedUrls()
                    .stream()
                    .map(failedUrl -> fetcherFunction.apply(failedUrl, visitedIssueUrls))
                    .reduce(new FetchingResult<>(), (acc, i) -> FetchingResult.union(i, acc));

        } else {
            return new FetchingResult<>();
        }
    }

    /**
     * Re-tries to fetch failed Comment URLs from the run(s) before this one. Ensures that no URLs
     * are visited that we _have_ seen during this run.
     *
     * @param fetcherFunction      a function that does the actual fetching
     * @param formerFetchingResult the result from the former run
     * @param newFetchingResult    the results we've seen so far during this run
     * @return the new results
     */
    private List<Pair<Issue, FetchingResult<Comment>>> retryCommentsFetching(
            BiFunction<String, List<String>, FetchingResult<Comment>> fetcherFunction,
            List<Pair<Issue, FetchingResult<Comment>>> formerFetchingResult,
            List<Pair<Issue, FetchingResult<Comment>>> newFetchingResult) {
        if (formerFetchingResult != null) {
            // get all the formerly visited URLs:
            final List<String> formerlyVisitedUrls = formerFetchingResult
                    .stream()
                    .map(p -> p.getSecond().getVisitedUrls())
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            // get all the newly visited URLs:
            final List<String> newlyVisitedUrls = (newFetchingResult == null)
                    ? new LinkedList<>()
                    : newFetchingResult
                    .stream()
                    .map(p -> p.getSecond().getVisitedUrls())
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            // combine the old and newly visited URLs:
            final List<String> visitedCommentUrls = io.vavr.collection.List
                    .ofAll(formerlyVisitedUrls)
                    .appendAll(newlyVisitedUrls)
                    .toJavaList();

            // retry fetching all failed issue URLs:
            log.debug("Trying to re-fetch formerly failed URLs...");
            return formerFetchingResult
                    .stream()
                    .map(p -> {
                        final Issue issue = p.getFirst();
                        final FetchingResult<Comment> fetchingResultsForSingleIssue = p
                                .getSecond()
                                .getFailedUrls()
                                .stream()
                                .map(failedUrl -> fetcherFunction.apply(failedUrl, visitedCommentUrls))
                                .reduce(new FetchingResult<>(), (acc, i) -> FetchingResult.union(i, acc));
                        return new Pair<>(issue, fetchingResultsForSingleIssue);
                    })
                    .collect(Collectors.toList());

        } else {
            return new LinkedList<>();
        }
    }

    private Pair<File, File> condorizeIssuesAndUsers(
            List<Issue> issues,
            List<Pair<Issue, List<Comment>>> comments,
            String outputDir) {

        // aggregate all users over all issues and their comments:
        final List<User> users = aggregateUsers(issues, comments);

        // fetch additional info about those users:
        final List<User> fullBlownUsers = users
                .parallelStream()
                .map(fetcher::fetchAllInfoForUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                // we need at least an ID to prevent duplicates:
                .filter(u -> u.getId() != null && !u.getId().trim().isEmpty())
                .collect(Collectors.toList());

        // rectangularize:
        final List<Pair<Issue, Interaction<User, Comment>>> rectangularizedIssues = rectangularize(
                issues, comments);

        // map to edges (tickets) and nodes (persons), then write to CSV files:
        return CondorizorUtils.mapAndWriteToCsvFiles(
                fullBlownUsers,
                rectangularizedIssues,
                this::githubUsersToCondorActors,
                this::githubIssuesToCondorEdges,
                outputDir
        );
    }

    private List<User> aggregateUsers(List<Issue> issues, List<Pair<Issue, List<Comment>>> comments) {
        // get all ticket creators:
        final List<User> creators = issues
                .parallelStream()
                .map(Issue::getUser)
                .collect(Collectors.toList());

        // ...and commentators:
        final List<User> commentators = comments
                .parallelStream()
                .map(Pair::getSecond)
                .flatMap(List::stream)
                .map(Comment::getUser)
                .collect(Collectors.toList());

        // return the union:
        return io.vavr.collection.List
                .ofAll(creators)
                .appendAll(commentators)
                .distinctBy(User::getId)
                .toJavaList();
    }

    private List<Pair<Issue, Interaction<User, Comment>>> rectangularize(
            List<Issue> issues,
            List<Pair<Issue, List<Comment>>> comments) {
        // map all ticket creations to our pseudo-sum-type:
        final List<Pair<Issue, Interaction<User, Comment>>> creationInteractions = issues
                .parallelStream()
                .map(t -> new Pair<>(t,
                        new Interaction<User, Comment>(t.getUser(), null, EdgeType.CREATION)))
                .collect(Collectors.toList());

        // turn the List of Pair<Issue, List<Comment>> into a flattened List of Pair<Issue, Comment>
        // and map them to our pseudo-sum-type:
        final List<Pair<Issue, Interaction<User, Comment>>> commentInteractions = comments
                .parallelStream()
                .map(iwc -> {
                    final Issue issue = iwc.getFirst();
                    return iwc.getSecond()
                            .stream()
                            .map(c -> new Pair<Issue, Comment>(issue, c))
                            .collect(Collectors.toList());
                })
                .flatMap(List::stream)
                .map(p -> new Pair<Issue, Interaction<User, Comment>>(p.getFirst(),
                        new Interaction<User, Comment>(null, p.getSecond(), EdgeType.COMMENT)))
                .collect(Collectors.toList());

        // return the union:
        return io.vavr.collection.List
                .ofAll(creationInteractions)
                .appendAll(commentInteractions)
                // TODO: .appendAll(assigningInteractions)
                .toJavaList();
    }

    private List<Actor> githubUsersToCondorActors(List<User> repoUsers) {
        final String fallbackStartTime = TimeFormattingHelper.unixEpoch(); // TODO: calculate "starttime"!

        // eliminate duplicate users:
        final Stream<User> distinctGithubUsers = io.vavr.collection.List
                .ofAll(repoUsers)
                .distinctBy(User::getId)
                .toJavaStream();

        // map users to actors:
        return distinctGithubUsers
                .map(u -> new Actor(
                                u.getId(), u.getLogin(),
                                computeTimestamp(fallbackStartTime, null),
                                u.getCompany(), u.getLocation(),
                                u.getEmail(), u.isHireable()
                        )
                )
                .collect(Collectors.toList());
    }

    private List<Edge> githubIssuesToCondorEdges(List<Pair<Issue, Interaction<User, Comment>>> ticketInteractions) {
        final String fallbackStartTime = TimeFormattingHelper.unixEpoch();

        final Function<Pair<Issue, Interaction<User, Comment>>, Edge> toEdge = i -> {
            final Issue issue = i.getFirst();
            final User ticketAuthor = issue.getUser(); // the original ticket author
            final String startTime = computeTimestamp(issue.getCreatedAt(), fallbackStartTime);
            final String endTime = computeTimestamp(issue.getClosedAt(), null);

            final Edge edge = new Edge(
                    issue.getTitle(), UUID.randomUUID().toString(),
                    null, ticketAuthor.getId(),
                    startTime, endTime,
                    "", "",
                    issue.getState(), "",
                    issue.getComments(), "",
                    i.getSecond().getEdgeType()
                    // TODO: add an attribute "original_ID" that holds issue.getId() !
            );

            if (i.getSecond().getEdgeType() == EdgeType.CREATION) {
                edge.setSource(ticketAuthor.getId());
            }

            if (i.getSecond().getEdgeType() == EdgeType.COMMENT) {
                final Comment comment = i.getSecond().getComment();
                final User commentator = i.getSecond().getComment().getUser();
                final String escapedCommentBody = StringEscapeUtils
                        .escapeHtml4(comment.getBody())
                        .replaceAll("\r\n", " ")
                        .replaceAll("\n", " ");
                edge.setCommentBody(escapedCommentBody);
                edge.setSource(commentator.getId());
            }
            return edge;
        };

        return ticketInteractions
                .parallelStream()
                .map(toEdge)
                .collect(Collectors.toList());
    }

    private String computeTimestamp(String githubTimestamp, String fallbackTimestamp) {
        final String fbts = (fallbackTimestamp == null)
                ? TimeFormattingHelper.now()
                : fallbackTimestamp;

        final String ts = (githubTimestamp == null || githubTimestamp.trim().isEmpty())
                ? fbts
                : githubTimestamp;

        return TimeFormattingHelper.githubTimestampToCondorTimestamp(ts);
    }
}

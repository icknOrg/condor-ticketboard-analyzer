package org.coins1920.group05.condorizor;

import org.apache.commons.text.StringEscapeUtils;
import org.coins1920.group05.fetcher.FetchingResult;
import org.coins1920.group05.fetcher.GitHubIssueFetcher;
import org.coins1920.group05.model.condor.Actor;
import org.coins1920.group05.model.condor.Edge;
import org.coins1920.group05.model.condor.EdgeType;
import org.coins1920.group05.model.general.Interaction;
import org.coins1920.group05.model.github.rest.Comment;
import org.coins1920.group05.model.github.rest.Issue;
import org.coins1920.group05.model.github.rest.User;
import org.coins1920.group05.util.Pair;
import org.coins1920.group05.util.TimeFormattingHelper;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitHubRepoCondorizor {

    private final GitHubIssueFetcher fetcher;

    public GitHubRepoCondorizor(boolean paginate) {
        final String oauthToken = System.getenv("GITHUB_OAUTH_KEY");
        this.fetcher = new GitHubIssueFetcher(oauthToken, paginate);
    }

    public Pair<File, File> fetchGitHubIssues(String owner, String board, String outputDir) {
        // first, fetch all issues of the given repo:
        final boolean fetchClosedTickets = true;
        final FetchingResult<Issue> issueFetchingResult = fetcher.fetchTickets(owner, board, fetchClosedTickets);
        final List<Issue> githubIssues = issueFetchingResult.getEntities();

        // fetch all comments and the corresponding users for all issues:
        final List<Pair<Issue, List<Comment>>> comments = githubIssues
                .parallelStream()
                .map(i -> new Pair<>(i, fetcher.fetchCommentsForTicket(i)))
                .collect(Collectors.toList());

        // fetch all assignees for all tickets:
        final List<Pair<Issue, List<User>>> assignees = githubIssues
                .parallelStream()
                .map(i -> new Pair<>(i, fetcher.fetchAssigneesForTicket(i)))
                .collect(Collectors.toList());

        // aggregate all users, map to Condor Actors/Edges and write to CSV files:
        return condorizeIssuesAndUsers(githubIssues, comments, outputDir);
    }

    private Pair<File, File> condorizeIssuesAndUsers(
            List<Issue> issues,
            List<Pair<Issue, List<Comment>>> comments,
            String outputDir) {

        // aggregate all users over all issues and their comments:
        final List<User> users = aggregateUsers(issues, comments);

        // fetch additional infos about those users:
        final List<User> fullBlownUsers = users
                .parallelStream()
                .map(fetcher::fetchAllInfosForUser)
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

        return ticketInteractions
                .parallelStream()
                .map(i -> {
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
                })
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

package org.coins1920.group05.condorizor;

import org.coins1920.group05.fetcher.GitHubIssueFetcher;
import org.coins1920.group05.fetcher.model.condor.Actor;
import org.coins1920.group05.fetcher.model.condor.Edge;
import org.coins1920.group05.fetcher.model.github.Issue;
import org.coins1920.group05.fetcher.model.github.User;
import org.coins1920.group05.fetcher.util.Pair;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitHubRepoCondorizor {

    public Pair<File, File> fetchGitHubIssues(String owner, String board, String outputDir) {
        final String apiKey = System.getenv("GITHUB_API_KEY");
        final String oauthToken = System.getenv("GITHUB_OAUTH_KEY");
        final GitHubIssueFetcher fetcher = new GitHubIssueFetcher(apiKey, oauthToken);

        // fetch all repo contributors: in GitHub, there is not a single "getAllUsersForTicket"-like
        // endpoint. We rather have to combine the results of multiple ones:
        final List<User> githubRepoContributors = fetcher.fetchBoardMembers(owner, board);

        // fetch all issues of the given repo:
        final Stream<Issue> githubIssues = fetcher
                .fetchTickets(owner, board)
                .stream();

        // the final data set should be "rectangular", i.e. a ticket/card tuple is duplicated
        // for _every_ member that changed it, wrote a comment, etc.:
        final List<Issue> githubIssuesForAllAuthors = githubIssues
                .map(i -> duplicateIssueForAllParticipatingUsers(i, fetcher.fetchMembersForTicket(owner, board, i.getId())))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // map to edges (tickets) and nodes (persons), then write to CSV files:
        return CondorizorUtils.mapAndWriteToCsvFiles(
                githubRepoContributors,
                githubIssuesForAllAuthors,
                this::githubUsersToCondorActors,
                this::githubIssuesToCondorEdges,
                outputDir
        );
    }

    private List<Issue> duplicateIssueForAllParticipatingUsers(Issue issue, List<User> contributors) {
        return new LinkedList<>(); // TODO: ...
    }


    private List<Actor> githubUsersToCondorActors(List<User> githubRepoUsers) {
        return new LinkedList<>(); // TODO: ...
    }

    private List<Edge> githubIssuesToCondorEdges(List<Issue> issues) {
        return new LinkedList<>(); // TODO: ...
    }
}

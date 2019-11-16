package org.coins1920.group05.condorizor;

import org.coins1920.group05.fetcher.GitHubIssueFetcher;
import org.coins1920.group05.fetcher.model.condor.Actor;
import org.coins1920.group05.fetcher.model.condor.Edge;
import org.coins1920.group05.fetcher.model.condor.EdgeType;
import org.coins1920.group05.fetcher.model.general.CategorizedBoardMembers;
import org.coins1920.group05.fetcher.model.github.Issue;
import org.coins1920.group05.fetcher.model.github.User;
import org.coins1920.group05.fetcher.util.Pair;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitHubRepoCondorizor {

    public Pair<File, File> fetchGitHubIssues(String owner, String board, String outputDir) {
        final String apiKey = System.getenv("GITHUB_API_KEY");
        final String oauthToken = System.getenv("GITHUB_OAUTH_KEY");
        final GitHubIssueFetcher fetcher = new GitHubIssueFetcher(apiKey, oauthToken);

        // first, fetch all issues of the given repo:
        final Stream<Issue> githubIssues = fetcher
                .fetchTickets(owner, board)
                .stream();

        // for all issues: fetch all users that participated (as assignee or commentator or ...):
        final Function<Issue, Pair<Issue, CategorizedBoardMembers<User>>> withAllParticipatingUsers = i ->
                new Pair<>(i,
                        new CategorizedBoardMembers<User>(
                                i.getUser(),
                                fetcher.fetchAssigneesForTicket(i),
                                fetcher.fetchCommentatorsForTicket(i)
                        ));
        final List<Pair<Issue, CategorizedBoardMembers<User>>> issuesWithUsers = githubIssues
                .map(withAllParticipatingUsers)
                .collect(Collectors.toList());

        // fetch all repo contributors (i.e. people who committed or merged PRs):
//        final List<User> githubRepoContributors = fetcher.fetchBoardMembers(owner, board); // TODO: are committers relevant for the issues?

        // aggregate all users, map to Condor Actors/Edges and write to CSV files:
        return condorizeIssuesAndUsers(issuesWithUsers, outputDir);
    }

    private Pair<File, File> condorizeIssuesAndUsers(
            List<Pair<Issue, CategorizedBoardMembers<User>>> issuesWithUsers,
            String outputDir) {
        // aggregate all users over all issues:
        final List<User> allUsers = issuesWithUsers
                .stream()
                .map(Pair::getSecond)
                .map(cu -> io.vavr.collection.List
                        .ofAll(cu.getAssignees())
                        .appendAll(cu.getCommentators())
                        .append(cu.getCreator())
                        .toJavaList()
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // map to edges (tickets) and nodes (persons), then write to CSV files:
        return CondorizorUtils.mapAndWriteToCsvFiles(
                allUsers,
                issuesWithUsers,
                this::githubUsersToCondorActors,
                this::githubIssuesToCondorEdges,
                outputDir
        );
    }

    private List<Actor> githubUsersToCondorActors(List<User> repoUsers) {
        final String fakeStartDate = "2010-09-12T04:00:00+00:00"; // TODO: calculate "starttime"!

        // eliminate duplicate users:
        final Stream<User> distinctGithubUsers = io.vavr.collection.List
                .ofAll(repoUsers)
                .distinctBy(User::getId)
                .toJavaStream();

        // map users to actors:
        return distinctGithubUsers
                .map(u -> new Actor(u.getId(), u.getLogin(), fakeStartDate))
                .collect(Collectors.toList());
    }

    private List<Edge> githubIssuesToCondorEdges(List<Pair<Issue, CategorizedBoardMembers<User>>> issues) {
        final Function<User, Edge> userToActor = u -> new Edge("", "", "", "",
                "", "", "", "", "", "",
                "", EdgeType.REACTION);

        return new LinkedList<>(); // TODO: ...
    }
}

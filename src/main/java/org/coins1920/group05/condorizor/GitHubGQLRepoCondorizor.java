package org.coins1920.group05.condorizor;

import org.coins1920.group05.fetcher.GitHubGQLIssueFetcher;
import java.io.File;
import org.coins1920.group05.util.Pair;

import java.util.stream.Stream;

public class GitHubGQLRepoCondorizor {
    public Pair<File, File> fetchGitHubIssues(String owner, String board, String outputDir) {
        final String oauthToken = System.getenv("GITHUB_OAUTH_KEY");
        final GitHubGQLIssueFetcher fetcher = new GitHubGQLIssueFetcher(oauthToken);

        // fetch objects
        final Stream<Object> githubIssues = fetcher
                .fetch(owner, board)
                .stream();

        //TODO: Ggf. JSON Mapping
        //TODO: CSVs erstellen


        return null;
    }
}

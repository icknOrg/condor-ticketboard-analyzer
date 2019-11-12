package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.github.Interaction;
import org.coins1920.group05.fetcher.model.github.Issue;
import org.coins1920.group05.fetcher.model.github.Repo;
import org.coins1920.group05.fetcher.model.github.User;
import org.coins1920.group05.fetcher.util.RestClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GitHubIssueFetcher implements TicketBoardFetcher<Repo, User, Issue, Interaction> {

    private static final String GITHUB_ROOT_URI = "https://api.github.com/";

    private Logger logger = LoggerFactory.getLogger(GitHubIssueFetcher.class);

    private final RestTemplate rt;
    private String key;
    private String token;

    public GitHubIssueFetcher() {
        this.rt = new RestTemplateBuilder()
                .rootUri(GITHUB_ROOT_URI)
                .build();
    }

    public GitHubIssueFetcher(String apiKey, String oauthToken) {
        this.key = apiKey;
        this.token = oauthToken;
        this.rt = new RestTemplateBuilder()
                .rootUri(GITHUB_ROOT_URI)
                .build();
    }

    @Override
    public List<Repo> fetchBoards() {
        return null;
    }

    @Override
    public Repo fetchBoard(String owner, String board) {
        return null;
    }

    @Override
    public List<User> fetchBoardMembers(String owner, String board) {
        final String url = "/repos/{owner}/{board}/contributors";
        final ResponseEntity<User[]> response = rt.getForEntity(url, User[].class, owner, board);
        // TODO: pagination! Consecutively add '...?page=2&per_page=100' to the URL!
        return RestClientHelper.nonNullResponseEntities(response);
    }

    @Override
    public List<Issue> fetchTickets(String owner, String board) {
        final String url = "/repos/{owner}/{board}/issues";
        final ResponseEntity<Issue[]> response = rt.getForEntity(url, Issue[].class, owner, board);
        // TODO: pagination! Consecutively add '...?page=2&per_page=100' to the URL!
        return RestClientHelper.nonNullResponseEntities(response);
    }

    @Override
    public List<Interaction> fetchActionsForTicket(String ticketId) {
        // TODO: curl 'https://api.github.com/repos/linuxmint/cinnamon-spices-extensions/issues/198/events'
        return null;
    }

    @Override
    public List<User> fetchMembersForTicket(String owner, String board, String ticketId) {
        final List<User> contributors = new LinkedList<>();

        // to get ALL GitHub users that participated in an issue, we first get all its assignees:
        final String url = "/repos/{owner}/{board}/issues/{ticketId}";
        final ResponseEntity<Issue> response = rt.getForEntity(url, Issue.class, owner, board, ticketId);
        if (response.getBody() != null) {
            contributors.addAll(Arrays.asList(response.getBody().getAssignees()));
        }

        // ...then all those users who wrote a comment:
        // TODO: curl 'https://api.github.com/repos/linuxmint/cinnamon-spices-extensions/issues/220/comments'

        // and finally everyone who reacted (e.g. by emoji-liking a comment:
        // TODO: fetchActionsForTicket().getUsers()

        return contributors;
    }

}

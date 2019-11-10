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
        return null;
    }

    @Override
    public List<User> fetchMembersForTicket(String ticketId) {
        return null;
    }

}

package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.github.*;
import org.coins1920.group05.fetcher.util.RestClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GitHubIssueFetcher implements TicketBoardFetcher<Repo, User, Issue, Event> {

    private static final String GITHUB_ROOT_URI = "https://api.github.com/";

    private Logger logger = LoggerFactory.getLogger(GitHubIssueFetcher.class);

    private final RestTemplate rt;
    private String oauthToken;

    public GitHubIssueFetcher() {
        this.rt = new RestTemplateBuilder()
                .rootUri(GITHUB_ROOT_URI)
                .build();
    }

    public GitHubIssueFetcher(String oauthToken) {
        this.oauthToken = oauthToken;
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
        final ResponseEntity<User[]> response = getAllEntitiesWithPagination(url, User[].class, owner, board);
        return RestClientHelper.nonNullResponseEntities(response);
    }

    @Override
    public List<Issue> fetchTickets(String owner, String board) {
        // all open tickets:
        final String openTicketsUrl = "/repos/{owner}/{board}/issues";
        final ResponseEntity<Issue[]> openTicketsResponse = getAllEntitiesWithPagination(
                openTicketsUrl, Issue[].class, owner, board);

        // and all closed ones:
        final String closedTicketsUrl = "/repos/{owner}/{board}/issues?state=closed";
        final ResponseEntity<Issue[]> closedTicketsResponse = getAllEntitiesWithPagination(
                closedTicketsUrl, Issue[].class, owner, board);

        return io.vavr.collection.List
                .ofAll(RestClientHelper.nonNullResponseEntities(openTicketsResponse))
                .appendAll(RestClientHelper.nonNullResponseEntities(closedTicketsResponse))
                .toJavaList();
    }

    @Override
    public List<Event> fetchActionsForTicket(String ticketId) {
        // TODO: curl 'https://api.github.com/repos/linuxmint/cinnamon-spices-extensions/issues/198/events'
        return null;
    }

    @Override
    public List<User> fetchMembersForTicket(Issue ticket) {
        final List<User> contributors = new LinkedList<>();

        // to get ALL GitHub users that participated in an issue, we first get all its assignees:
        contributors.addAll(fetchAssigneesForTicket(ticket));

        // ...then all those users who wrote a comment:
        contributors.addAll(fetchCommentatorsForTicket(ticket));

        // and finally everyone who reacted (e.g. by emoji-liking a comment:
        // TODO: fetchActionsForTicket().getUsers()

        return contributors;
    }

    @Override
    public List<User> fetchAssigneesForTicket(Issue ticket) {
        final ResponseEntity<Issue> response = getAllEntitiesWithPagination(ticket.getUrl(), Issue.class);

        if (response.getBody() == null) {
            return new LinkedList<>();
        } else {
            logger.info("I got " + response.getBody().getAssignees().length + " assignee(s)!");
            return Arrays
                    .stream(response.getBody().getAssignees())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<User> fetchCommentatorsForTicket(Issue ticket) {
        if (ticket.getCommentsUrl() == null || ticket.getCommentsUrl().isEmpty()) {
            logger.debug("  the issue " + ticket.getId() + " has no comments => no comments URL!");
            return new LinkedList<>();
        } else {
            // TODO: instead of using a separate RT, we should strip the root URI off of the getCommentsUrl() string!
            try {
                final String commentsUrl = new URL(ticket.getCommentsUrl()).getPath();
                final ResponseEntity<Comment[]> commentsResponse = getAllEntitiesWithPagination(commentsUrl, Comment[].class);
                final List<Comment> comments = RestClientHelper.nonNullResponseEntities(commentsResponse);
                return comments
                        .stream()
                        .filter(Objects::nonNull)
                        .map(Comment::getUser) // TODO: this is a bad idea, as the "created-at" timestamp info is lost!
                        .collect(Collectors.toList());

            } catch (MalformedURLException e) {
                logger.error("The comments URL ('" + ticket.getCommentsUrl() +
                        "') for ticket " + ticket.getId() + "was malformed!", e);
                return new LinkedList<>();
            }
        }
    }

    private <T> ResponseEntity<T> getAllEntitiesWithPagination(String url, Class<T> responseType, Object... uriVariables) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "Spring RestTemplate");
        headers.set("Authorization", "token " + this.oauthToken);

        final HttpEntity<?> entity = new HttpEntity<>(headers);
        final ResponseEntity<T> response = rt.exchange(url, HttpMethod.GET, entity, responseType, uriVariables);

        // TODO: paginate!
        // TODO: consecutively add '...?page=2&per_page=100' to the URL!

        return response;
    }
}

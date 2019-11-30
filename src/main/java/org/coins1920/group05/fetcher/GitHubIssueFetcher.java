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
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class GitHubIssueFetcher implements TicketBoardFetcher<Repo, User, Issue, Event, Comment> {

    private static final Logger logger = LoggerFactory.getLogger(GitHubIssueFetcher.class);
    private static final String GITHUB_ROOT_URI = "https://api.github.com/";

    private final RestTemplate rt;
    private final String oauthToken;
    private final Boolean paginate;

    public GitHubIssueFetcher(String oauthToken, boolean paginate) {
        this.oauthToken = oauthToken;
        this.paginate = paginate;
        this.rt = new RestTemplateBuilder()
                .rootUri(GITHUB_ROOT_URI)
                .build();
    }

    public GitHubIssueFetcher(String oauthToken, boolean paginate, String url) {
        this.oauthToken = oauthToken;
        this.paginate = paginate;
        this.rt = new RestTemplateBuilder()
                .rootUri(url)
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
        return getAllEntitiesWithPagination((u, e) ->
                rt.exchange(u, HttpMethod.GET, e, User[].class, owner, board), url);
    }

    @Override
    public List<Issue> fetchTickets(String owner, String board, boolean fetchClosedTickets) {
        // all open tickets:
        final String openTicketsUrl = "/repos/{owner}/{board}/issues";
        final List<Issue> openIssuesList = getAllEntitiesWithPagination((u, e) ->
                rt.exchange(u, HttpMethod.GET, e, Issue[].class, owner, board), openTicketsUrl);
        logger.debug("I got " + openIssuesList.size() + " issues!");

        // and all closed ones:
        final String closedTicketsUrl = "/repos/{owner}/{board}/issues?state=closed";
        final List<Issue> closedIssuesList = getAllEntitiesWithPagination((u, e) ->
                rt.exchange(u, HttpMethod.GET, e, Issue[].class, owner, board), closedTicketsUrl);
        logger.debug("I got " + closedIssuesList.size() + " closed issues!");

        return io.vavr.collection.List
                .ofAll(openIssuesList)
                .appendAll(closedIssuesList)
                // filter out all PRs, we only want issues:
                // TODO: .filter(i -> i.getPullRequest() == null || i.getPullRequest().getUrl() == null)
                // TODO: the "pull_request" object in the JSON response is not the right property to distinguish issues from PRs!
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
        final ResponseEntity<Issue> response = rt.getForEntity(ticket.getUrl(), Issue.class);

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
        return fetchCommentsForTicket(ticket)
                .stream()
                .filter(Objects::nonNull)
                .map(Comment::getUser)
                .collect(Collectors.toList());
    }

    @Override
    public List<Comment> fetchCommentsForTicket(Issue ticket) {
        if (ticket.getCommentsUrl() == null || ticket.getCommentsUrl().isEmpty()) {
            logger.warn("  the issue " + ticket.getId() + " has no comments => no comments URL!");
            return new LinkedList<>();
        } else {
            try {
                final String commentsUrl = new URL(ticket.getCommentsUrl()).getPath();
                return getAllEntitiesWithPagination((u, e) ->
                        rt.exchange(u, HttpMethod.GET, e, Comment[].class), commentsUrl);
            } catch (MalformedURLException e) {
                logger.error("The comments URL ('" + ticket.getCommentsUrl() +
                        "') for ticket " + ticket.getId() + "was malformed!", e);
                return new LinkedList<>();
            }
        }
    }

    private <U> List<U> getAllEntitiesWithPagination(BiFunction<String, HttpEntity<?>, ResponseEntity<U[]>> f, String url) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "Spring RestTemplate");
        headers.set("Authorization", "token " + this.oauthToken);

        final HttpEntity<?> entity = new HttpEntity<>(headers);
        final ResponseEntity<U[]> response = f.apply(url, entity);

        final String paginationLinkKey = "Link";
        if (paginate && response.getHeaders().containsKey(paginationLinkKey)) {
            final String linkUrls = Objects.requireNonNull(
                    response.getHeaders().get(paginationLinkKey)).get(0);
            final Optional<String> linkUrlOptional = RestClientHelper
                    .splitGithubPaginationLinks(linkUrls);
            if (linkUrlOptional.isPresent()) {
                final String linkUrl = linkUrlOptional.get();
                logger.debug("Found a link to the next page: " + linkUrl);
                return io.vavr.collection.List
                        .ofAll(RestClientHelper.nonNullResponseEntities(response))
                        .appendAll(getAllEntitiesWithPagination(f, linkUrl))
                        .toJavaList();
            } else {
                return RestClientHelper.nonNullResponseEntities(response);
            }
        } else {
            return RestClientHelper.nonNullResponseEntities(response);
        }
    }
}

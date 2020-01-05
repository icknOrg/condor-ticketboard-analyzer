package org.coins1920.group05.fetcher;

import lombok.extern.slf4j.Slf4j;
import org.coins1920.group05.model.github.rest.*;
import org.coins1920.group05.util.RestClientHelper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
public class GitHubIssueFetcher implements TicketBoardFetcher<Repo, User, Issue, Event, Comment> {

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
                rt.exchange(u, HttpMethod.GET, e, User[].class, owner, board), url)
                .getEntities();
    }

    @Override
    public FetchingResult<Issue> fetchTickets(String owner, String board, boolean fetchClosedTickets, List<String> visitedUrls, List<String> failedUrls) {
        // all open tickets:
        final String openTicketsUrl = "/repos/{owner}/{board}/issues";
        final FetchingResult<Issue> openIssues = getAllEntitiesWithPagination((u, e) ->
                rt.exchange(u, HttpMethod.GET, e, Issue[].class, owner, board), openTicketsUrl);
        log.debug("I got " + openIssues.getEntities().size() + " issues!");

        // and all closed ones?
        if (!fetchClosedTickets) {
            // no, just the open ones:
            return openIssues;

        } else {
            final String closedTicketsUrl = "/repos/{owner}/{board}/issues?state=closed";
            final FetchingResult<Issue> closedIssues = getAllEntitiesWithPagination((u, e) ->
                    rt.exchange(u, HttpMethod.GET, e, Issue[].class, owner, board), closedTicketsUrl);
            log.debug("I got " + closedIssues.getEntities().size() + " closed issues!");

            // filter out all PRs, we only want issues: // TODO: do we??
            // TODO: .filter(i -> i.getPullRequest() == null || i.getPullRequest().getUrl() == null)
            // TODO: the "pull_request" object in the JSON response is not the right property to distinguish issues from PRs!
            return FetchingResult.union(openIssues, closedIssues);
        }
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
        return Arrays.asList(ticket.getAssignees());
    }

    @Override
    public List<User> fetchCommentatorsForTicket(Issue ticket) {
        return fetchCommentsForTicket(ticket)
                .getEntities()
                .stream()
                .filter(Objects::nonNull)
                .map(Comment::getUser)
                .collect(Collectors.toList());
    }

    @Override
    public FetchingResult<Comment> fetchCommentsForTicket(Issue ticket) {
        if (ticket.getCommentsUrl() == null || ticket.getCommentsUrl().isEmpty()) {
            log.warn("  the issue " + ticket.getId() + " has no comments => no comments URL!");
            return new FetchingResult<>();
        } else {
            try {
                final String commentsUrl = new URL(ticket.getCommentsUrl()).getPath();
                return getAllEntitiesWithPagination((u, e) ->
                        rt.exchange(u, HttpMethod.GET, e, Comment[].class), commentsUrl);

            } catch (MalformedURLException e) {
                log.error("The comments URL ('" + ticket.getCommentsUrl() +
                        "') for ticket " + ticket.getId() + "was malformed!", e);
                return new FetchingResult<>();
            }
        }
    }

    @Override
    public User fetchAllInfosForUser(User user) {
        try {
            final ResponseEntity<User> responseEntity = rt
                    .exchange(user.getUrl(), HttpMethod.GET, httpEntityWithDefaultHeaders(), User.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity.getBody();
            } else {
                log.warn("I couldn't fetch the user for URL: " + user.getUrl());
                return null;
            }

        } catch (Exception e) {
            log.warn("Something went wrong: ", e);
            return null;
        }
    }

    private <U> FetchingResult<U> getAllEntitiesWithPagination(BiFunction<String, HttpEntity<?>, ResponseEntity<U[]>> f, String url) {
        final HttpEntity<?> entity = httpEntityWithDefaultHeaders();

        try {
            final ResponseEntity<U[]> response = f.apply(url, entity);
            final List<U> responseEntities = RestClientHelper.nonNullResponseEntities(response);

            final String paginationLinkKey = "Link";
            if (paginate && response.getHeaders().containsKey(paginationLinkKey)) {
                final String linkUrls = Objects.requireNonNull(
                        response.getHeaders().get(paginationLinkKey)).get(0);
                final Optional<String> linkUrlOptional = RestClientHelper
                        .splitGithubPaginationLinks(linkUrls);
                if (linkUrlOptional.isPresent()) {
                    final String linkUrl = linkUrlOptional.get();
                    log.debug("Found a link to the next page: " + linkUrl);

                    try {
                        final FetchingResult<U> fetchingResult = new FetchingResult<>(
                                responseEntities,
                                false,
                                new LinkedList<>(),
                                io.vavr.collection.List.of(url).toJavaList()
                        );
                        return FetchingResult.union(
                                fetchingResult,
                                getAllEntitiesWithPagination(f, linkUrl)
                        );

                        // catch 403 Forbidden exception for pagination requests:
                    } catch (HttpClientErrorException e) {
                        return new FetchingResult<>(
                                responseEntities,
                                true,
                                new LinkedList<>(), // TODO: URL list?
                                io.vavr.collection.List.of(url).toJavaList() // TODO: URL list?
                        );
                    }

                } else {
                    // there was no pagination link:
                    return new FetchingResult<>(
                            responseEntities,
                            false,
                            new LinkedList<>(), // TODO: URL list?
                            io.vavr.collection.List.of(url).toJavaList() // TODO: URL list?
                    );
                }
            } else {
                // there was no pagination link header:
                return new FetchingResult<>(
                        responseEntities,
                        false,
                        new LinkedList<>(), // TODO: URL list?
                        io.vavr.collection.List.of(url).toJavaList() // TODO: URL list?
                );
            }

        } catch (HttpClientErrorException eo) {
            return new FetchingResult<U>(
                    new LinkedList<>(),
                    true,
                    new LinkedList<>(), // TODO: URL list?
                    io.vavr.collection.List.of(url).toJavaList()
            );
        }

    }

    private HttpEntity<?> httpEntityWithDefaultHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "Spring RestTemplate");
        headers.set("Authorization", "token " + this.oauthToken);
        return new HttpEntity<>(headers);
    }
}

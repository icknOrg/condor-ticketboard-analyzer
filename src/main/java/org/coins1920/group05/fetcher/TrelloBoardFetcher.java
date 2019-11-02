package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.trello.Action;
import org.coins1920.group05.fetcher.model.trello.Board;
import org.coins1920.group05.fetcher.model.trello.Card;
import org.coins1920.group05.fetcher.model.trello.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TrelloBoardFetcher implements TicketBoardFetcher {

    private Logger logger = LoggerFactory.getLogger(TrelloBoardFetcher.class);

    private final RestTemplate rt;
    private final String key;
    private final String token;

    public TrelloBoardFetcher(String key, String token) {
        this.key = key;
        this.token = token;
        this.rt = new RestTemplateBuilder()
                .rootUri("https://api.trello.com/")
                .build();
    }

    @Override
    public List<Board> fetchBoards() {
        final String url = assembleUrl("members/me/boards", null);
        final ResponseEntity<Board[]> response = rt.getForEntity(url, Board[].class, key, token);
        return nonNullResponseEntities(response);
    }

    @Override
    public Board fetchBoard(String boardId) {
        final String url = assembleUrl("boards/{boardId}", null);
        final ResponseEntity<Board> response = rt.getForEntity(url, Board.class, boardId, key, token);
        return response.getBody();
    }

    @Override
    public List<Member> fetchBoardMembers(String boardId) {
        final String url = assembleUrl("boards/{boardId}/members", null);
        final ResponseEntity<Member[]> response = rt.getForEntity(url, Member[].class, boardId, key, token);
        return nonNullResponseEntities(response);
    }

    @Override
    public List<Card> fetchTickets(String boardId) {
        final String url = assembleUrl("boards/{boardId}/cards", null);
        final ResponseEntity<Card[]> response = rt.getForEntity(url, Card[].class, boardId, key, token);
        return nonNullResponseEntities(response);
    }

    @Override
    public List<Action> fetchActionsForTicket(String ticketId) {
        final String url = assembleUrl("cards/{ticketId}/actions", "&filter=all");
        final ResponseEntity<Action[]> response = rt.getForEntity(url, Action[].class, ticketId, key, token);
        return nonNullResponseEntities(response);
    }

    @Override
    public List<Member> fetchMembersForTicket(String ticketId) {
        final String url = assembleUrl("cards/{ticketId}/members", null);
        final ResponseEntity<Member[]> response = rt.getForEntity(url, Member[].class, ticketId, key, token);
        return nonNullResponseEntities(response);
    }

    private String assembleUrl(String resourcePart, String urlParameters) {
        final String nonNullUrlParameters = (urlParameters == null) ? "" : urlParameters;
        return "/1/" + resourcePart + "?key={key}&token={token}" + nonNullUrlParameters;
    }

    private <T> List<T> nonNullResponseEntities(ResponseEntity<T[]> response) {
        if (response.getBody() == null) {
            return new LinkedList<>();
        } else {
            logger.info("I got " + response.getBody().length + " item(s)!");
            return Arrays
                    .stream(response.getBody())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

}

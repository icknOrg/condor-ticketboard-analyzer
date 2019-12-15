package org.coins1920.group05.fetcher;

import org.coins1920.group05.model.trello.*;
import org.coins1920.group05.util.RestClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;
import java.util.List;

public class TrelloBoardFetcher implements TicketBoardFetcher<Board, Member, Card, Action, Comment> {

    private static final Logger logger = LoggerFactory.getLogger(TrelloBoardFetcher.class);
    private static final String TRELLO_ROOT_URI = "https://api.trello.com/";

    private final RestTemplate rt;
    private final String key;
    private final String token;

    public TrelloBoardFetcher(String key, String token) {
        this.key = key;
        this.token = token;
        this.rt = new RestTemplateBuilder()
                .rootUri(TRELLO_ROOT_URI)
                .build();
    }

    public TrelloBoardFetcher(String key, String token, String url) {
        this.key = key;
        this.token = token;
        this.rt = new RestTemplateBuilder()
                .rootUri(url)
                .build();
    }

    @Override
    public List<Board> fetchBoards() {
        final String url = assembleUrl("members/me/boards", null);
        final ResponseEntity<Board[]> response = rt.getForEntity(url, Board[].class, key, token);
        return RestClientHelper.nonNullResponseEntities(response);
    }

    @Override
    public Board fetchBoard(String owner, String board) {
        final String url = assembleUrl("boards/{board}", null);
        final ResponseEntity<Board> response = rt.getForEntity(url, Board.class, board, key, token);
        return response.getBody();
    }

    @Override
    public List<Member> fetchBoardMembers(String owner, String board) {
        final String url = assembleUrl("boards/{board}/members", null);
        final ResponseEntity<Member[]> response = rt.getForEntity(url, Member[].class, board, key, token);
        return RestClientHelper.nonNullResponseEntities(response);
    }

    @Override
    public List<Card> fetchTickets(String owner, String board, boolean fetchClosedTickets) {
        final String url = assembleUrl("boards/{board}/cards", null);
        final ResponseEntity<Card[]> response = rt.getForEntity(url, Card[].class, board, key, token);
        return RestClientHelper.nonNullResponseEntities(response);
    }

    @Override
    public List<Action> fetchActionsForTicket(String ticketId) {
        final String url = assembleUrl("cards/{ticketId}/actions", "&filter=all");
        final ResponseEntity<Action[]> response = rt.getForEntity(url, Action[].class, ticketId, key, token);
        return RestClientHelper.nonNullResponseEntities(response);
    }

    @Override
    public List<Member> fetchMembersForTicket(Card ticket) {
        final String ticketId = ticket.getId();
        final String url = assembleUrl("cards/{ticketId}/members", null);
        final ResponseEntity<Member[]> response = rt.getForEntity(url, Member[].class, ticketId, key, token);
        return RestClientHelper.nonNullResponseEntities(response);
    }

    @Override
    public List<Member> fetchAssigneesForTicket(Card ticket) {
        logger.warn("The operation 'fetchAssigneesForTicket()' is not yet supported and will return an empty list!");
        return new LinkedList<>();
    }

    @Override
    public List<Member> fetchCommentatorsForTicket(Card ticket) {
        logger.warn("The operation 'fetchCommentatorsForTicket()' is not yet supported and will return an empty list!");
        return new LinkedList<>();
    }

    @Override
    public List<Comment> fetchCommentsForTicket(Card ticket) {
        logger.warn("The operation 'fetchAssigneesForTicket()' is not yet supported and will return an empty list!");
        return new LinkedList<>();
    }

    @Override
    public Member fetchAllInfosForUser(Member user) {
        logger.warn("The operation 'fetchAllInfosForUser()' is not yet supported!");
        return null;
    }

    private String assembleUrl(String resourcePart, String urlParameters) {
        final String nonNullUrlParameters = (urlParameters == null) ? "" : urlParameters;
        return "/1/" + resourcePart + "?key={key}&token={token}" + nonNullUrlParameters;
    }
}

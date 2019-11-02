package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.trello.Board;
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

    public TrelloBoardFetcher(TicketBoard tbt, String key, String token) {
        // TODO: distinguish between Trello, Jira, ... implementations (via "tbt")!
        this.key = key;
        this.token = token;
        this.rt = new RestTemplateBuilder()
                .rootUri("https://api.trello.com/")
                .build();
    }

    @Override
    public List<Board> fetchBoards() {
        final String url = assembleUrl("members/me/boards");
        final ResponseEntity<Board[]> response = rt.getForEntity(url, Board[].class, key, token);
        logger.info("HTTP status code waa: " + response.getStatusCodeValue());
        return nonNullResponseEntities(response);
    }

    @Override
    public Board fetchBoard(String boardId) {
        final String url = assembleUrl("boards/{id}");
        final ResponseEntity<Board> response = rt.getForEntity(url, Board.class, boardId, key, token);
        logger.info("HTTP status code was: " + response.getStatusCodeValue());
        return response.getBody();
    }

    @Override
    public List<Member> fetchBoardMembers(String boardId) {
        final String url = assembleUrl("boards/{id}/members");
        final ResponseEntity<Member[]> response = rt.getForEntity(url, Member[].class, boardId, key, token);
        logger.info("HTTP status codewas : " + response.getStatusCodeValue());
        return nonNullResponseEntities(response);
    }

    private String assembleUrl(String resourcePart) {
        return "/1/" + resourcePart + "?key={key}&token={token}";
    }

    private <T> List<T> nonNullResponseEntities(ResponseEntity<T[]> response) {
        if (response.getBody() == null) {
            return new LinkedList<>();
        } else {
            return Arrays
                    .stream(response.getBody())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

}

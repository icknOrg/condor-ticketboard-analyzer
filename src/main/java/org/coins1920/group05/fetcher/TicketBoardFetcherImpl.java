package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.trello.Board;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class TicketBoardFetcherImpl implements TicketBoardFetcher {

    private final RestTemplate rt;
    private final String key;
    private final String token;

    public TicketBoardFetcherImpl(TicketBoard tbt, String key, String token) {
        // TODO: distinguish between Trello, Jira, ... implementations (via "tbt")!
        this.key = key;
        this.token = token;
        this.rt = new RestTemplateBuilder()
                .rootUri("https://api.trello.com/")
                .build();
    }

    @Override
    public List<Board> fetchBoards() {
        final String url = "1/members/me/boards?key={key}&token={token}"; // TODO: hide root URL in RestTemplateBuilder
        final ResponseEntity<Board[]> response = rt.getForEntity(url, Board[].class, key, token);
        System.out.println("Status code: " + response.getStatusCodeValue()); // TODO: slf4j and a logger impl for test

        if (response.getBody() == null) {
            return new LinkedList<>();
        } else {
            return Arrays
                    .stream(response.getBody())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Board fetchBoard(String boardId) {
        final String url = assembleUrl("boards/{id}");
        final ResponseEntity<Board> response = rt.getForEntity(url, Board.class, boardId, key, token);
        return response.getBody();
    }

    @Override
    public List<String> fetchBoardMembers(String boardId) {
        final String url = assembleUrl("boards/{id}/member");
        final ResponseEntity<Board> response = rt.getForEntity(url, Board.class, boardId, key, token);
        System.out.println("\n\n --> " + response.getStatusCodeValue());
        System.out.println("\n\n --> " + response.getBody());
        return null;
    }

    private String assembleUrl(String resourcePart) {
        return "/1/" + resourcePart + "?key={key}&token={token}";
    }

}

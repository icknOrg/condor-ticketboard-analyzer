package org.coins1920.group05;

import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.fetcher.TicketBoardFetcher;
import org.coins1920.group05.fetcher.TicketBoardFetcherImpl;
import org.coins1920.group05.fetcher.model.trello.Board;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TrelloFetcherTest {

    private static TicketBoardFetcher fetcher;

    @BeforeClass
    public static void setUp() {
        final String key = System.getenv("trello-api-key");
        final String token = System.getenv("trello-oauth-token");
        fetcher = new TicketBoardFetcherImpl(TicketBoard.TRELLO, key, token);
    }

    @Test
    public void testFetchSingleBoard() {
        final Board board = fetcher.fetchBoard("lgaJQMYA");
        assertThat(board, is(not(nullValue())));
        assertThat(board.getId(), is("5db19ed8f8b54324663c159c"));
        assertThat(board.getName(), is("Team5Coin"));
    }

    @Test
    @Ignore
    public void testFetchBoardMembers() {
        fetcher.fetchBoardMembers("lgaJQMYA");
//        assertThat(board, is(not(nullValue())));
//        assertThat(board.getId(), is("5db19ed8f8b54324663c159c"));
//        assertThat(board.getName(), is("Team5Coin"));
    }

}

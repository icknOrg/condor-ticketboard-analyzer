package org.coins1920.group05;

import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.fetcher.TicketBoardFetcher;
import org.coins1920.group05.fetcher.TicketBoardFetcherImpl;
import org.coins1920.group05.fetcher.model.trello.Board;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for the ticket board fetching app itself.
 */
public class AppTest {

    // TODO: test the app class instead!
    @Test
    @Ignore
    public void testBoardFetching() {
        final String key = "..."; // TODO: load API credentials from environment variable
        final String token = "..."; // TODO: load API credentials from environment variable
        final TicketBoardFetcher f = new TicketBoardFetcherImpl(TicketBoard.TRELLO, key, token);

        final List<Board> boards = f.fetchBoards();
        assertThat(boards, is(not(nullValue())));
        assertThat(boards.size(), is(not(0)));
        System.out.println("There are " + boards.size() + "boards");

        final Board firstBoard = boards.get(0);
        System.out.println("boards[0] is: " + firstBoard);
        System.out.println("boards[0].id = " + firstBoard.getId());
    }

}

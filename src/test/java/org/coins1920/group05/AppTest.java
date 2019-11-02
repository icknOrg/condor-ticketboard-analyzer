package org.coins1920.group05;

import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.fetcher.TicketBoardFetcher;
import org.coins1920.group05.fetcher.TicketBoardFetcherImpl;
import org.coins1920.group05.fetcher.model.trello.Board;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for the ticket board fetching app itself.
 */
public class AppTest {

    private Logger logger = LoggerFactory.getLogger(AppTest.class);

    private static TicketBoardFetcher fetcher;

    @BeforeClass
    public static void setUp() {
        final String key = System.getenv("TRELLO_API_KEY");
        final String token = System.getenv("TRELLO_OAUTH_KEY");
        fetcher = new TicketBoardFetcherImpl(TicketBoard.TRELLO, key, token);
    }

    // TODO: test the app class instead!
    @Test
//    @Ignore
    public void testBoardFetching() {
        final List<Board> boards = fetcher.fetchBoards();
        assertThat(boards, is(not(nullValue())));
        assertThat(boards.size(), is(not(0)));
        logger.info("There are " + boards.size() + " boards!");

        final Board firstBoard = boards.get(0);
        logger.info("boards[0] is: " + firstBoard);
        logger.info("boards[0].id = " + firstBoard.getId());
    }

}

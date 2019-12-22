package org.coins1920.group05;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.coins1920.group05.fetcher.FetchingResult;
import org.coins1920.group05.fetcher.TrelloBoardFetcher;
import org.coins1920.group05.model.trello.Action;
import org.coins1920.group05.model.trello.Board;
import org.coins1920.group05.model.trello.Card;
import org.coins1920.group05.model.trello.Member;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TrelloFetcherTest {

    private Logger logger = LoggerFactory.getLogger(TrelloFetcherTest.class);

    private static final String SAMPLE_BOARD_SHORTLINK = "lgaJQMYA";
    private static final String SAMPLE_CARD_ID1 = "5db19ed82bd7cd5b26346bd7";
    private static final String SAMPLE_CARD_ID2 = "5db19ed8256e14829baf66e0";
    private static final int WIREMOCK_PORT = 8089;

    private static TrelloBoardFetcher fetcher;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(WIREMOCK_PORT));

    @BeforeClass
    public static void setUpClass() {
        final String key = System.getenv("TRELLO_API_KEY");
        final String token = System.getenv("TRELLO_OAUTH_KEY");
        final String wiremockUrl = "http://localhost:" + WIREMOCK_PORT + "/";
        fetcher = new TrelloBoardFetcher(key, token, wiremockUrl);
    }

    @Before
    public void setUp() {
        final String singleBoard = TestUtils.readFromResourceFile(
                "trello/single_board.json", TrelloFetcherTest.class);
        stubFor(get(urlPathMatching("/1/boards/" + SAMPLE_BOARD_SHORTLINK + "([a-zA-Z0-9/-]*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", TestUtils.APPLICATION_JSON)
                        .withBody(singleBoard)));

        final String boardMembers = TestUtils.readFromResourceFile(
                "trello/board_members.json", TrelloFetcherTest.class);
        stubFor(get(urlPathMatching("/1/boards/" + SAMPLE_BOARD_SHORTLINK + "/members" + "([a-zA-Z0-9/-]*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", TestUtils.APPLICATION_JSON)
                        .withBody(boardMembers)));

        final String cards = TestUtils.readFromResourceFile(
                "trello/cards.json", TrelloFetcherTest.class);
        stubFor(get(urlPathMatching("/1/boards/" + SAMPLE_BOARD_SHORTLINK + "/cards" + "([a-zA-Z0-9/-]*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", TestUtils.APPLICATION_JSON)
                        .withBody(cards)));

        final String cardActions = TestUtils.readFromResourceFile(
                "trello/card_actions.json", TrelloFetcherTest.class);
        stubFor(get(urlPathMatching("/1/cards/" + SAMPLE_CARD_ID1 + "/actions" + "([a-zA-Z0-9/-]*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", TestUtils.APPLICATION_JSON)
                        .withBody(cardActions)));

        final String cardMembers = TestUtils.readFromResourceFile(
                "trello/card_members.json", TrelloFetcherTest.class);
        stubFor(get(urlPathMatching("/1/cards/" + SAMPLE_CARD_ID2 + "/members" + "([a-zA-Z0-9/-]*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", TestUtils.APPLICATION_JSON)
                        .withBody(cardMembers)));
    }

    @Test
    public void testFetchSingleBoard() {
        final Board board = fetcher.fetchBoard(null, SAMPLE_BOARD_SHORTLINK);
        assertThat(board, is(not(nullValue())));
        assertThat(board.getId(), is("5db19ed8f8b54324663c159c"));
        assertThat(board.getName(), is("Team5Coin"));
    }

    @Test
    public void testFetchBoardMembers() {
        final List<Member> members = fetcher.fetchBoardMembers(null, SAMPLE_BOARD_SHORTLINK);
        assertThat(members, is(not(nullValue())));
        assertThat(members.size(), is(not(0)));
        logger.info("There is/are " + members.size() + " member(s)!");
        logger.info(" the first one is: " + members.get(0));

        final long membersCalledBugs = members
                .stream()
                .filter(m -> m.getFullName().equals("Bugs"))
                .count();
        assertThat(membersCalledBugs, is(1L));
    }

    @Test
    public void testFetchCards() {
        final FetchingResult<Card> cardFetchingResult = fetcher
                .fetchTickets(null, SAMPLE_BOARD_SHORTLINK, false);
        final List<Card> cards = cardFetchingResult.getEntities();
        assertThat(cards, is(not(nullValue())));
        assertThat(cards.size(), is(not(0)));
        logger.info("There is/are " + cards.size() + " card(s)!");
        logger.info(" the first one is: " + cards.get(0));

        final long cardsNamedRCsvStructure = cards
                .stream()
                .filter(m -> m.getName().equals("CSV structure"))
                .count();
        assertThat(cardsNamedRCsvStructure, is(1L));
    }

    @Test
    public void testFetchActionsForCard() {
        final List<Action> actions = fetcher.fetchActionsForTicket(SAMPLE_CARD_ID1);
        assertThat(actions, is(not(nullValue())));
        assertThat(actions.size(), is(not(0)));
        logger.info("There is/are " + actions.size() + " action(s)!");
        logger.info(" the first one is: " + actions.get(0));

        final long actionsWithCreateCardType = actions
                .stream()
                .filter(a -> a.getType().equals("createCard"))
                .count();
        assertThat(actionsWithCreateCardType, is(1L));
    }

    @Test
    public void testFetchMembersForCard() {
        final Card card = new Card();
        card.setId(SAMPLE_CARD_ID2);

        final List<Member> members = fetcher.fetchMembersForTicket(card);
        assertThat(members, is(not(nullValue())));
        assertThat(members.size(), is(not(0)));
        logger.info("There is/are " + members.size() + " member(s)!");
        logger.info(" the first one is: " + members.get(0));

        final long membersWithUsernamePp89 = members
                .stream()
                .filter(a -> a.getUsername().equals("patrickp89"))
                .count();
        assertThat(membersWithUsernamePp89, is(1L));
    }

    @Test
    @Ignore
    public void testFetchAllBoardsOfAMember() {
        final List<Board> boards = fetcher.fetchBoards();
        assertThat(boards, is(not(nullValue())));
        assertThat(boards.size(), is(not(0)));
        logger.info("There are " + boards.size() + " boards!");

        final Board firstBoard = boards.get(0);
        logger.info("boards[0] is: " + firstBoard);
        logger.info("boards[0].id = " + firstBoard.getId());
    }
}

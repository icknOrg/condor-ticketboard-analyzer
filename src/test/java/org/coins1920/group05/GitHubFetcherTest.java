package org.coins1920.group05;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.coins1920.group05.fetcher.GitHubIssueFetcher;
import org.coins1920.group05.fetcher.model.github.Issue;
import org.coins1920.group05.fetcher.model.github.User;
import org.coins1920.group05.fetcher.util.RestClientHelper;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GitHubFetcherTest {

    private static final Logger logger = LoggerFactory.getLogger(GitHubFetcherTest.class);

    private static final String SAMPLE_BOARD_OWNER = "linuxmint";
    private static final String SAMPLE_BOARD_NAME1 = "cinnamon-spices-extensions";
    private static final int WIREMOCK_PORT = 8089;

    private static GitHubIssueFetcher fetcher;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(WIREMOCK_PORT));

    @BeforeClass
    public static void setUpClass() {
        final String oauthToken = System.getenv("GITHUB_OAUTH_KEY");
        final String wiremockUrl = "http://localhost:" + WIREMOCK_PORT + "/";
        final boolean paginate = false;
        fetcher = new GitHubIssueFetcher(oauthToken, paginate, wiremockUrl);
    }

    @Before
    public void setUp() {
        final String openIssues = TestUtils.readFromResourceFile(
                "github/issues.json", GitHubFetcherTest.class);
        stubFor(get(urlEqualTo("/repos/" + SAMPLE_BOARD_OWNER + "/" + SAMPLE_BOARD_NAME1 + "/issues"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", TestUtils.APPLICATION_JSON)
                        .withBody(openIssues)));

        final String closedIssues = TestUtils.readFromResourceFile(
                "github/closed_issues_p01.json", GitHubFetcherTest.class);
        stubFor(get(urlPathEqualTo("/repos/" + SAMPLE_BOARD_OWNER + "/" + SAMPLE_BOARD_NAME1 + "/issues"))
                .withQueryParam("state", matching("closed"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", TestUtils.APPLICATION_JSON)
                        // TODO: add link to p2!
                        .withBody(closedIssues)));
    }

    @Test
    public void testFetchIssues() {
        final List<Issue> issues = fetcher.fetchTickets(SAMPLE_BOARD_OWNER, SAMPLE_BOARD_NAME1, false);
        assertThat(issues, is(not(nullValue())));
        logger.info("There is/are " + issues.size() + " issue(s)!");
        assertThat(issues.size(), is(46));
        // 46 is the unpaginated (!) result of: (16 open tickets) + (30 closed ones) = 46 !
        // TODO: fix the expected size of the issue collection, once pagination is implemented!
        logger.info(" the first one is: " + issues.get(0));
    }

    @Test
    @Ignore // TODO: add a Wiremock stub!
    public void testFetchRepoContributors() {
        final Issue issue = new Issue();
        issue.setId("476356409");
        issue.setCommentsUrl("https://api.github.com/repos/linuxmint/cinnamon-spices-extensions/issues/220/comments");

        final List<User> commentators = fetcher.fetchCommentatorsForTicket(issue);
        assertThat(commentators, is(not(nullValue())));
        assertThat(commentators.size(), is(not(0)));
        logger.info("There is/are " + commentators.size() + " commentator(s)!");
        logger.info(" the first one is: " + commentators.get(0));
    }

    @Test
    @Ignore // TODO: add a Wiremock stub!
    public void testFetchIssueContributors() {
        final Issue issue = new Issue();
        issue.setId("7168");

        final List<User> contributors = fetcher.fetchMembersForTicket(issue);
        assertThat(contributors, is(not(nullValue())));
        assertThat(contributors.size(), is(not(0)));
        logger.info("There is/are " + contributors.size() + " contributor(s)!");
        logger.info(" the first one is: " + contributors.get(0));

        final long contributorsCalledClefebvre = contributors
                .stream()
                .filter(a -> a.getLogin().equals("clefebvre"))
                .count();
        assertThat(contributorsCalledClefebvre, is(1L));
    }

    @Test
    public void testPaginationLinkSplitting() {
        final String links = "<https://api.github.com/repositories/79458054/issues?state=" +
                "closed&page=2>; rel=\"next\", <https://api.github.com/repositories/79458054/issues?state=closed&page=8>; rel=\"last\"";

        final Optional<String> nextPageLink = RestClientHelper.splitGithubPaginationLinks(links);
        assertThat(nextPageLink, is(not(nullValue())));

        final String link = nextPageLink.orElseGet(() -> null);
        assertThat(link, is(not(nullValue())));

        final String expectedLink = "https://api.github.com/repositories/79458054/issues?state=closed&page=2";
        assertThat(link, is(expectedLink));
    }

    @Test
    public void testPaginationLinkSplittingWithThreeLinks() {
        final String links = "<https://api.github.com/repositories/79458054/issues?state=" +
                "closed&page=1>; rel=\"prev\", <https://api.github.com/repositories/79458054/issues?state=closed&page=3>; " +
                "rel=\"next\", <https://api.github.com/repositories/79458054/issues?state=closed&page=8>; " +
                "rel=\"last\", <https://api.github.com/repositories/79458054/issues?state=closed&page=1>; rel=\"first\"";

        final Optional<String> nextPageLink = RestClientHelper.splitGithubPaginationLinks(links);
        assertThat(nextPageLink, is(not(nullValue())));

        final String link = nextPageLink.orElseGet(() -> null);
        assertThat(link, is(not(nullValue())));

        final String expectedLink = "https://api.github.com/repositories/79458054/issues?state=closed&page=3";
        assertThat(link, is(expectedLink));
    }

    @Test
    public void testPaginationLinkSplittingWithNoNextLink() {
        final String links = "Link: <https://api.github.com/repositories/79458054/issues?state=closed&" +
                "page=7>; rel=\"prev\", <https://api.github.com/repositories/79458054/issues?state=closed&page=1>; rel=\"first\"";

        final Optional<String> nextPageLink = RestClientHelper.splitGithubPaginationLinks(links);
        assertThat(nextPageLink, is(not(nullValue())));
        assertThat(nextPageLink.isPresent(), is(false));
    }
}

package org.coins1920.group05;

import org.coins1920.group05.fetcher.GitHubIssueFetcher;
import org.coins1920.group05.fetcher.model.github.Issue;
import org.coins1920.group05.fetcher.model.github.User;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GitHubFetcherTest {

    private Logger logger = LoggerFactory.getLogger(GitHubFetcherTest.class);

    private static final String SAMPLE_BOARD_OWNER = "linuxmint";
    private static final String SAMPLE_BOARD_NAME1 = "cinnamon-spices-extensions";
    private static final String SAMPLE_BOARD_NAME2 = "cinnamon";
    private static GitHubIssueFetcher fetcher;

    @BeforeClass
    public static void setUpClass() {
        fetcher = new GitHubIssueFetcher();
    }

    @Test
    public void testFetchIssues() {
        final List<Issue> issues = fetcher.fetchTickets(SAMPLE_BOARD_OWNER, SAMPLE_BOARD_NAME1);
        assertThat(issues, is(not(nullValue())));
        assertThat(issues.size(), is(not(0)));
        logger.info("There is/are " + issues.size() + " issue(s)!");
        logger.info(" the first one is: " + issues.get(0));
    }

    @Test
    public void testFetchRepoContributors() {
        final List<User> contributors = fetcher.fetchBoardMembers(SAMPLE_BOARD_OWNER, SAMPLE_BOARD_NAME1);
        assertThat(contributors, is(not(nullValue())));
        assertThat(contributors.size(), is(not(0)));
        logger.info("There is/are " + contributors.size() + " contributor(s)!");
        logger.info(" the first one is: " + contributors.get(0));
    }

    @Test
    public void testFetchIssueContributors() {
        final List<User> contributors = fetcher.fetchMembersForTicket(
                SAMPLE_BOARD_OWNER,
                SAMPLE_BOARD_NAME2,
                "7168"
        );
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
}

package org.coins1920.group05;

import org.coins1920.group05.fetcher.GitHubIssueFetcher;
import org.coins1920.group05.fetcher.model.github.Issue;
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
    private static final String SAMPLE_BOARD_NAME = "cinnamon-spices-extensions";
    private static GitHubIssueFetcher fetcher;

    @BeforeClass
    public static void setUpClass() {
        fetcher = new GitHubIssueFetcher();
    }

    @Test
    public void testFetchCards() {
        final List<Issue> issues = fetcher.fetchTickets(SAMPLE_BOARD_OWNER, SAMPLE_BOARD_NAME);
        assertThat(issues, is(not(nullValue())));
        assertThat(issues.size(), is(not(0)));
        logger.info("There is/are " + issues.size() + " issue(s)!");
        logger.info(" the first one is: " + issues.get(0));
    }
}

package org.coins1920.group05;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

/**
 * Tests for the ticket board fetching app itself.
 */
public class AppTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    @Ignore
    public void testTrelloBoardFetching() throws IOException {
        final File testFolder = temporaryFolder.newFolder("test-condor-csv-files");
        final String boardId = "lgaJQMYA";
        final String[] args = {"trello", boardId, testFolder.getAbsolutePath()};
        TicketBoardFetcherApp.main(args);
    }

    @Test
    @Ignore
    public void testGitHubRepoFetching() throws IOException {
        final File testFolder = temporaryFolder.newFolder("test-condor-csv-files");
        final String owner = "linuxmint";
        final String repo = "cinnamon-spices-extensions";
        final String[] args = {"github", owner, repo, testFolder.getAbsolutePath()};
        TicketBoardFetcherApp.main(args);
    }
}

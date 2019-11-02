package org.coins1920.group05;

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
    public void testBoardFetching() throws IOException {
        final File testFolder = temporaryFolder.newFolder("test-condor-csv-files");
        final String boardId = "lgaJQMYA";
        final String[] args = {"trello", boardId, testFolder.getAbsolutePath()};
        TicketBoardFetcherApp.main(args);
    }

}

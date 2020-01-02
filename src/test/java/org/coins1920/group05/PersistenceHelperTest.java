package org.coins1920.group05;

import org.coins1920.group05.util.PersistenceHelper;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PersistenceHelperTest {

    private final String testPartialResultFileName1 =
            "partial/linuxmint-cinnamon-spices-applets-2019-12-07T14:11:51Z.partial";

    private final String testPartialResultFileName2 =
            "partial/linuxmint-cinnamon-spices-applets-2020-01-02T14:11:51Z.partial";

    @Test
    public void testPartialResultDetection() throws IOException {
        final File partialResultFile = TestUtils.getFileFromResourceFolder(testPartialResultFileName1, PersistenceHelperTest.class);
        assertThat(partialResultFile, is(not(nullValue())));

        final String owner = "linuxmint";
        final String board = "cinnamon-spices-applets";
        final String outputDir = partialResultFile.getParent();
        assertThat(outputDir, is(not(nullValue())));

        boolean partialResultExists = PersistenceHelper.checkForPartialResult(owner, board, outputDir);
        assertThat(partialResultExists, is(true));
    }

    @Test
    public void testTsComputationFromPartialResultFileName() throws FileNotFoundException {
        final File partialResultFile1 = TestUtils.getFileFromResourceFolder(testPartialResultFileName1, PersistenceHelperTest.class);
        assertThat(partialResultFile1, is(not(nullValue())));

        final File partialResultFile2 = TestUtils.getFileFromResourceFolder(testPartialResultFileName2, PersistenceHelperTest.class);
        assertThat(partialResultFile2, is(not(nullValue())));

        final Long ts1 = PersistenceHelper.computeTsFromPartialResultFileName(partialResultFile1.toPath());
        assertThat(ts1, is(not(nullValue())));

        final Long ts2 = PersistenceHelper.computeTsFromPartialResultFileName(partialResultFile2.toPath());
        assertThat(ts2, is(not(nullValue())));

        assertThat(ts2, is(greaterThan(ts1)));
    }
}

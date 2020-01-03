package org.coins1920.group05;

import org.coins1920.group05.fetcher.FetchingResult;
import org.coins1920.group05.fetcher.PartialFetchingResult;
import org.coins1920.group05.model.github.rest.Comment;
import org.coins1920.group05.model.github.rest.Issue;
import org.coins1920.group05.model.github.rest.User;
import org.coins1920.group05.util.Pair;
import org.coins1920.group05.util.PersistenceHelper;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PersistenceHelperTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final String owner = "linuxmint";
    private final String board = "cinnamon-spices-applets";
    private final String resourceFolder = "partial/";
    private final String testPartialResultFileName1 = "linuxmint-cinnamon-spices-applets-2019-12-07T14:11:51Z.partial";
    private final String testPartialResultFileName2 = "linuxmint-cinnamon-spices-applets-2020-01-02T14:11:51Z.partial";

    @Test
    public void testPartialResultDetection() throws IOException {
        final File partialResultFile = TestUtils
                .getFileFromResourceFolder(resourceFolder + testPartialResultFileName1, PersistenceHelperTest.class);
        assertThat(partialResultFile, is(not(nullValue())));

        final String outputDir = partialResultFile.getParent();
        assertThat(outputDir, is(not(nullValue())));

        boolean partialResultExists = PersistenceHelper.checkForPartialResult(owner, board, outputDir);
        assertThat(partialResultExists, is(true));
    }

    @Test
    public void testTsComputationFromPartialResultFileName() throws FileNotFoundException {
        final File partialResultFile1 = TestUtils
                .getFileFromResourceFolder(resourceFolder + testPartialResultFileName1, PersistenceHelperTest.class);
        assertThat(partialResultFile1, is(not(nullValue())));
        final File partialResultFile2 = TestUtils
                .getFileFromResourceFolder(resourceFolder + testPartialResultFileName2, PersistenceHelperTest.class);
        assertThat(partialResultFile2, is(not(nullValue())));

        final Long ts1 = PersistenceHelper.computeTsFromPartialResultFileName(partialResultFile1.toPath());
        assertThat(ts1, is(not(nullValue())));
        final Long ts2 = PersistenceHelper.computeTsFromPartialResultFileName(partialResultFile2.toPath());
        assertThat(ts2, is(not(nullValue())));

        assertThat(ts2, is(greaterThan(ts1)));
    }

    @Test
    public void testPersistedPartialResultReading() throws IOException, ClassNotFoundException {
        final File partialResultFile2 = TestUtils
                .getFileFromResourceFolder(resourceFolder + testPartialResultFileName2, PersistenceHelperTest.class);
        assertThat(partialResultFile2, is(not(nullValue())));

        final String outputDir = partialResultFile2.getParent();
        assertThat(outputDir, is(not(nullValue())));

        final PartialFetchingResult<Issue, User, Comment> partialResult = PersistenceHelper
                .readPersistedPartialResult(owner, board, outputDir);
        assertThat(partialResult, is(not(nullValue())));

        assertThat(partialResult.getIssueFetchingResult(), is(not(nullValue())));
        System.out.println("  CommentsFetchingResults().size = " + partialResult.getIssueFetchingResult().getEntities().size());
        assertThat(partialResult.getCommentsFetchingResults(), is(not(nullValue())));
        System.out.println("  CommentsFetchingResults().size = " + partialResult.getCommentsFetchingResults().size());
    }

    @Test
    @Ignore
    public void testPersistPartialResult() throws IOException {
        final File folder = temporaryFolder.newFolder();
        final String outputDir = folder.getAbsolutePath();
        final PartialFetchingResult<Issue, User, Comment> partialFetchingResult = testResult();

        final File file = PersistenceHelper
                .persistPartialResultsToDisk(partialFetchingResult, owner, board, outputDir);
        assertThat(file, is(not(nullValue())));
    }

    private PartialFetchingResult<Issue, User, Comment> testResult() {
        final List<Issue> issues = testIssues();
        final FetchingResult<Issue> issueFetchingResult = new FetchingResult<>(issues);
        final List<Pair<Issue, FetchingResult<Comment>>> commentsFetchingResults = testComments(issues);
        return new PartialFetchingResult<Issue, User, Comment>(issueFetchingResult, commentsFetchingResults);
    }

    private List<Pair<Issue, FetchingResult<Comment>>> testComments(List<Issue> issues) {
        return issues.stream()
                .map(i -> new Pair<Issue, FetchingResult<Comment>>(i, getTestCommentForIssue(i)))
                .collect(Collectors.toList());
    }

    private FetchingResult<Comment> getTestCommentForIssue(Issue issue) {
        // TODO: create comments...
        return null;
    }

    private List<Issue> testIssues() {
        final List<Issue> issues = new LinkedList<>();

        final Issue issue1 = new Issue();
        issue1.setNumber("12345");
        issue1.setTitle("Test Title 1");
        issue1.setUrl("http:/i.am.a/url");
        issues.add(issue1);

        final Issue issue2 = new Issue();
        issue2.setNumber("76543");
        issue2.setTitle("Super Title 2");
        issue2.setUrl("http:/gonna.persist.them/all");
        issues.add(issue2);

        return issues;
    }
}

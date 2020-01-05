package org.coins1920.group05.util;

import lombok.extern.slf4j.Slf4j;
import org.coins1920.group05.fetcher.FetchingResult;
import org.coins1920.group05.fetcher.PartialFetchingResult;
import org.coins1920.group05.model.github.rest.Comment;
import org.coins1920.group05.model.github.rest.Issue;
import org.coins1920.group05.model.github.rest.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A helper class for persisting partial fetching results to disc.
 *
 * @author Patrick Preuß (patrickp89)
 * @author Julian Cornea (buggitheclown)
 */
@Slf4j
public class PersistenceHelper {

    private static final String PARTIAL_FILE_FILENAME_POSTFIX = ".partial";

    public static synchronized File persistPartialResultsToDisk(
            PartialFetchingResult<Issue, User, Comment> partialFetchingResult,
            String owner, String board, String outputDir) throws IOException {
        final String ts = TimeFormattingHelper.now();
        final String fileName = owner + "-" + board + "-" + ts + PARTIAL_FILE_FILENAME_POSTFIX;
        final File partialResult = new File(outputDir, fileName);

        final ObjectOutputStream outputStream = new ObjectOutputStream(
                new FileOutputStream(partialResult)
        );
        outputStream.writeObject(nonNullify(partialFetchingResult));
        outputStream.flush();
        outputStream.close();

        return partialResult;
    }


    /**
     * Prevents null values to be written to disc.
     *
     * @param partialFetchingResult the partial fetching result
     * @return a PartialFetchingResult without null values
     */
    private static PartialFetchingResult<Issue, User, Comment> nonNullify(
            PartialFetchingResult<Issue, User, Comment> partialFetchingResult) {
        if (partialFetchingResult == null) {
            partialFetchingResult = new PartialFetchingResult<>();
        }
        if (partialFetchingResult.getIssueFetchingResult() == null) {
            partialFetchingResult.setIssueFetchingResult(new FetchingResult<>());
        }
        if (partialFetchingResult.getCommentsFetchingResults() == null) {
            partialFetchingResult.setCommentsFetchingResults(new LinkedList<>());
        }
        return partialFetchingResult;
    }


    public static PartialFetchingResult<Issue, User, Comment> readPersistedPartialResult(
            String owner, String board, String outputDir) throws IOException, ClassNotFoundException {
        final String fileNamePrefix = owner + "-" + board + "-";
        final List<Path> matchingFiles = getMatchingFiles(fileNamePrefix, outputDir);

        switch (matchingFiles.size()) {
            case 0:
                throw new IllegalStateException("There are no matching files in '" + outputDir + "'!");
            case 1:
                return deserialize(matchingFiles.get(0));
            default:
                log.debug("There are multiple files matching '" + owner + "', '" + board + "':");
                return pickNewestPartialResultFile(matchingFiles);
        }
    }


    private static PartialFetchingResult<Issue, User, Comment> pickNewestPartialResultFile(
            List<Path> matchingFiles) throws IOException, ClassNotFoundException {
        matchingFiles.forEach(mf -> log.debug(" " + mf.getFileName()));
        // pick the file with the newest timestamp:
        final Path newestPartial = matchingFiles
                .stream()
                .map(p -> new Pair<>(computeTsFromPartialResultFileName(p), p))
                .max(Comparator.comparingLong(Pair::getFirst))
                .orElseThrow(() -> new IllegalStateException(""))
                .getSecond();

        log.debug("I picked the newest one: " + newestPartial.getFileName().toString());
        return deserialize(newestPartial);
    }


    @SuppressWarnings("unchecked") // TODO: in Java 8, there's no chance to type-safely cast generic wrappers... :(
    public static PartialFetchingResult<Issue, User, Comment> deserialize(Path path) throws IOException, ClassNotFoundException {
        final FileInputStream fileInputStream = new FileInputStream(path.toFile());
        final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        final Object object = objectInputStream.readObject();
        return (PartialFetchingResult<Issue, User, Comment>) object;
    }


    public static Long computeTsFromPartialResultFileName(Path path) {
        final String fileName = path.getFileName().toString();
        final String regExp = ".{20}.partial";
        final Matcher matcher = Pattern
                .compile(regExp)
                .matcher(fileName);

        // TODO: readme! Since Java 9 there's a cool function: Matcher.results()
        // It makes the following super-ugly while loop unnecessary:
        final LinkedList<String> matches = new LinkedList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        // we chose Java 8 for compatibility reasons, but one should rather use Java 9+ ASAP!

        if (matches.size() == 1) {
            final String formattedTs = matches
                    .get(0)
                    .substring(0, 20);

            return TimeFormattingHelper
                    .parseIso8601Timestamp(formattedTs)
                    .getEpochSecond();

        } else {
            throw new IllegalStateException("Malformed file name!");
        }
    }


    public static boolean checkForPartialResult(String owner, String board, String outputDir) throws IOException {
        final String fileNamePrefix = owner + "-" + board + "-";

        log.debug("Looking for '" + fileNamePrefix + "*' in " + outputDir);
        final List<Path> matchingFiles = getMatchingFiles(fileNamePrefix, outputDir);

        matchingFiles.forEach(f -> log.debug("Found matching file: " + f));
        return matchingFiles.size() > 0;
    }


    public static List<Path> getMatchingFiles(String fileNamePrefix, String outputDir) throws IOException {
        return Files
                .walk(Paths.get(outputDir))
                .filter(p -> p.getFileName().toString().endsWith(PARTIAL_FILE_FILENAME_POSTFIX))
                .filter(p -> p.getFileName().toString().startsWith(fileNamePrefix))
                .collect(Collectors.toList());
    }
}

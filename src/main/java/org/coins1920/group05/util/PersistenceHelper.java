package org.coins1920.group05.util;

import org.coins1920.group05.fetcher.PartialFetchingResult;
import org.coins1920.group05.model.github.rest.Comment;
import org.coins1920.group05.model.github.rest.Issue;
import org.coins1920.group05.model.github.rest.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        outputStream.writeObject(partialFetchingResult);
        outputStream.flush();
        outputStream.close();

        return partialResult;
    }

    public static synchronized PartialFetchingResult<Issue, User, Comment> getPersistedPartialResult(
            String owner, String board, String outputDir) throws IOException {
        final String fileNamePrefix = owner + "-" + board + "-";
        final List<Path> matchingFiles = Files
                .walk(Paths.get(outputDir))
                .map(Path::getFileName)
                .filter(p -> p.endsWith(PARTIAL_FILE_FILENAME_POSTFIX))
                .filter(p -> p.startsWith(fileNamePrefix))
                .collect(Collectors.toList());

        if (matchingFiles.size() == 1) {
            return unserialize(matchingFiles.get(0));
        } else {
            // pick the file with the newest timestamp:
            final Path newestPartial = matchingFiles
                    .stream()
                    .map(p -> new Pair<>(computeTs(p), p))
                    .sorted(Comparator.comparingLong(Pair::getFirst))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(""))
                    .getSecond();

            return unserialize(newestPartial);
        }
    }

    public static PartialFetchingResult<Issue, User, Comment> unserialize(Path path) {
        // TODO: new ObjectInputStream() ...
        return null;
    }

    private static Long computeTs(Path p) {
        return null;
    }

    public static boolean checkForPartialResult(String owner, String board, String outputDir) throws IOException {
        final String fileNamePrefix = owner + "-" + board + "-";
        final List<Path> matchingFiles = Files
                .walk(Paths.get(outputDir))
                .filter(p -> p.endsWith(PARTIAL_FILE_FILENAME_POSTFIX))
                .filter(p -> p.startsWith(fileNamePrefix))
                .collect(Collectors.toList());

        if (matchingFiles.size() == 1) {
            return true;
        } else {
            throw new IllegalStateException("There are multiple matching *.partial files!");
        }
    }
}

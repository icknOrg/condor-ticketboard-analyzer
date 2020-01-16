package org.coins1920.group05.condorizor;

import org.coins1920.group05.fetcher.FetchingResult;
import org.coins1920.group05.model.condor.Actor;
import org.coins1920.group05.model.condor.Edge;
import org.coins1920.group05.model.general.AbstractComment;
import org.coins1920.group05.model.general.AbstractMember;
import org.coins1920.group05.model.general.AbstractTicket;
import org.coins1920.group05.model.general.Interaction;
import org.coins1920.group05.model.github.rest.Comment;
import org.coins1920.group05.model.github.rest.Issue;
import org.coins1920.group05.util.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A helper class that deals with combining Condor-related things.
 *
 * @author Patrick Preuß (patrickp89)
 * @author Julian Cornea (buggitheclown)
 */
public class CondorizorUtils {

    public static List<Pair<Issue, FetchingResult<Comment>>> combineCommentsFetchingResults(
            List<Pair<Issue, FetchingResult<Comment>>> fr1,
            List<Pair<Issue, FetchingResult<Comment>>> fr2) {
        final List<Pair<Issue, FetchingResult<Comment>>> fetchingResults1
                = (fr1 != null) ? fr1 : new LinkedList<>();
        final List<Pair<Issue, FetchingResult<Comment>>> fetchingResults2
                = (fr2 != null) ? fr2 : new LinkedList<>();

        final List<Issue> allIssues = io.vavr.collection.List
                .ofAll(fetchingResults1)
                .appendAll(fetchingResults2)
                .toJavaStream()
                .map(Pair::getFirst)
                .collect(Collectors.toList());

        return allIssues.stream()
                .map(i -> matchingIssueResultPairsForIssue(i, fetchingResults1, fetchingResults2))
                .flatMap(CondorizorUtils::foldMatchingResults)
                .collect(Collectors.toList());
    }

    private static List<Pair<Issue, FetchingResult<Comment>>> matchingIssueResultPairsForIssue(
            Issue i,
            List<Pair<Issue, FetchingResult<Comment>>> l1,
            List<Pair<Issue, FetchingResult<Comment>>> l2) {
        final List<Pair<Issue, FetchingResult<Comment>>> rl1 = l1
                .stream()
                .filter(fr -> fr.getFirst() == i)
                .collect(Collectors.toList());

        final List<Pair<Issue, FetchingResult<Comment>>> rl2 = l2
                .stream()
                .filter(fr -> fr.getFirst() == i)
                .collect(Collectors.toList());

        return io.vavr.collection.List
                .ofAll(rl1)
                .appendAll(rl2)
                .toJavaList();
    }

    private static Stream<Pair<Issue, FetchingResult<Comment>>> foldMatchingResults(
            List<Pair<Issue, FetchingResult<Comment>>> matches) {
        final Issue i = matches
                .get(0)
                .getFirst();

        final FetchingResult<Comment> combinedFetchingResult = matches
                .stream()
                .map(Pair::getSecond)
                .reduce(new FetchingResult<>(), (acc, c) -> FetchingResult.union(c, acc));

        return Stream.of(
                new Pair<>(i, combinedFetchingResult)
        );
    }


    /**
     * Combines n Issue FetchingResults into a single one.
     *
     * @param fetchingResults the results to combine
     * @return the combined one
     */
    @SafeVarargs
    public static FetchingResult<Issue> combineIssueFetchingResults(FetchingResult<Issue>... fetchingResults) {
        return Arrays
                .stream(fetchingResults)
                .reduce(new FetchingResult<>(), (acc, i) -> FetchingResult.union(i, acc));
    }


    /**
     * Maps tickets and board members to edges and actors. Writes them to the two CSV files.
     *
     * @param boardMembers a list of all known board members (incl. commentators etc.)
     * @param interactions a list of all ticket interactions associated with this board
     * @param toActors     a function that maps ticket interactions to edges
     * @param toEdges      a function that maps users to actors
     * @param outputDir    the directory where the CSV files should be written to
     * @param <M>          type parameter for members
     * @param <T>          type parameter for tickets
     * @param <C>          type param for comments
     * @return a pair of files (actors, tickets)
     */
    public static <M extends AbstractMember, T extends AbstractTicket, C extends AbstractComment> Pair<File, File> mapAndWriteToCsvFiles(
            List<M> boardMembers,
            List<Pair<T, Interaction<M, C>>> interactions,
            Function<List<M>, List<Actor>> toActors,
            Function<List<Pair<T, Interaction<M, C>>>, List<Edge>> toEdges, String outputDir) {
        // map to actors and tickets:
        final List<Actor> actors = toActors.apply(boardMembers);
        final List<Edge> edges = toEdges.apply(interactions);

        // write the CSV files to disc:
        final CondorCsvMarshaller condorCsvMarshaller = new DefaultCondorCsvMarshaller();
        return condorCsvMarshaller.write(actors, edges, outputDir);
    }


    @Deprecated // TODO: this is the "old" method used for Trello. The type parameters differ! -> get rid of it!
    public static <M extends AbstractMember, T extends AbstractTicket> Pair<File, File> mapAndWriteToCsvFilez(
            List<M> boardMembers,
            List<T> allTickets,
            Function<List<M>, List<Actor>> toActors,
            Function<List<T>, List<Edge>> toEdges, String outputDir) {
        // map to actors and tickets:
        final List<Actor> actors = toActors.apply(boardMembers);
        final List<Edge> edges = toEdges.apply(allTickets);

        // write the CSV files to disc:
        final CondorCsvMarshaller condorCsvMarshaller = new DefaultCondorCsvMarshaller();
        return condorCsvMarshaller.write(actors, edges, outputDir);
    }
}

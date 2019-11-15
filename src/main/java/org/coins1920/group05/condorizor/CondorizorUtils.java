package org.coins1920.group05.condorizor;

import org.coins1920.group05.fetcher.CondorCsvMarshaller;
import org.coins1920.group05.fetcher.DefaultCondorCsvMarshaller;
import org.coins1920.group05.fetcher.model.condor.Actor;
import org.coins1920.group05.fetcher.model.condor.Edge;
import org.coins1920.group05.fetcher.model.general.AbstractMember;
import org.coins1920.group05.fetcher.model.general.AbstractTicket;
import org.coins1920.group05.fetcher.util.Pair;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class CondorizorUtils {

    public static <M extends AbstractMember, T extends AbstractTicket> Pair<File, File> mapAndWriteToCsvFiles(
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

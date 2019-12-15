package org.coins1920.group05.condorizor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.coins1920.group05.model.condor.Actor;
import org.coins1920.group05.model.condor.Edge;
import org.coins1920.group05.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class DefaultCondorCsvMarshaller implements CondorCsvMarshaller {

    private static final String ACTORS_FILENAME = "persons.csv";
    private static final String TICKETS_FILENAME = "tickets.csv";
    private final CSVFormat condorFormat = CSVFormat.DEFAULT;

    @Override
    public Pair<File, File> write(List<Actor> actors, List<Edge> edges, String dir) {
        final File actorsFile = writeActors(actors, dir);
        final File ticketsFile = writeEdges(edges, dir);
        return new Pair<>(actorsFile, ticketsFile);
    }

    private File writeActors(List<Actor> actors, String dir) {
        final Path path = Paths.get(dir, ACTORS_FILENAME);
        final CSVFormat formatWithHeaders = condorFormat
                .withHeader(
                        "Id", "Name",
                        "starttime", "company",
                        "location", "email",
                        "hireable"
                );

        final BiConsumer<CSVPrinter, Actor> printActors = (csvp, p) -> {
            try {
                csvp.printRecord(
                        String.valueOf(p.getId()), p.getName(),
                        p.getStarttime(), p.getCompany(),
                        p.getLocation(), p.getEmail(),
                        p.getHireable()
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        return writeCsv(actors, printActors, formatWithHeaders, path);
    }

    private File writeEdges(List<Edge> edges, String dir) {
        final Path path = Paths.get(dir, TICKETS_FILENAME);
        final CSVFormat formatWithHeaders = condorFormat
                .withHeader(
                        "Name", "UUID",
                        "Source", "Target",
                        "Starttime", "Endtime",
                        "Planned_Duration", "Real_Duration",
                        "Status", "Count_Subtasks",
                        "Count_Comments", "Comment_Body",
                        "Edge_type"
                );

        final BiConsumer<CSVPrinter, Edge> printEdges = (csvp, t) -> {
            try {
                csvp.printRecord(t.getName(), t.getUuid(), t.getSource(), t.getTarget(),
                        t.getStartTime(), t.getEndTime(), t.getPlannedDuration(),
                        t.getRealDuration(), t.getStatus(), t.getCountSubtasks(),
                        t.getCountComments(), t.getCommentBody(),
                        t.getEdgeType().label);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        return writeCsv(edges, printEdges, formatWithHeaders, path);
    }

    private <T> File writeCsv(List<T> items, BiConsumer<CSVPrinter, T> printItems,
                              CSVFormat formatWithHeaders, Path path) {
        try (final BufferedWriter writer = Files.newBufferedWriter(path);
             final CSVPrinter csvPrinter = new CSVPrinter(writer, formatWithHeaders)) {
            items.stream()
                    .filter(Objects::nonNull)
                    .forEach(item -> printItems.accept(csvPrinter, item));
            return path.toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

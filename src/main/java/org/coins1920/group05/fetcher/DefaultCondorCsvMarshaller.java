package org.coins1920.group05.fetcher;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.coins1920.group05.fetcher.model.condor.Person;
import org.coins1920.group05.fetcher.model.condor.Ticket;
import org.coins1920.group05.fetcher.util.Pair;

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

    private static final String PERSONS_FILENAME = "persons.csv";
    private static final String TICKETS_FILENAME = "tickets.csv";
    private final CSVFormat condorFormat = CSVFormat.DEFAULT;

    @Override
    public Pair<File, File> write(List<Person> persons, List<Ticket> tickets, String dir) {
        final File personsFile = writePersons(persons, dir);
        final File ticketsFile = writeTickets(tickets, dir);
        return new Pair<>(personsFile, ticketsFile);
    }

    private File writePersons(List<Person> persons, String dir) {
        final Path path = Paths.get(dir, PERSONS_FILENAME);
        final CSVFormat formatWithHeaders = condorFormat
                .withHeader("Id", "Name", "starttime");

        final BiConsumer<CSVPrinter, Person> printPersons = (csvp, p) -> {
            try {
                csvp.printRecord(String.valueOf(p.getId()), p.getName(), p.getStarttime());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        return writeCsv(persons, printPersons, formatWithHeaders, path);
    }

    private File writeTickets(List<Ticket> tickets, String dir) {
        final Path path = Paths.get(dir, TICKETS_FILENAME);
        final CSVFormat formatWithHeaders = condorFormat
                .withHeader("Name", "UUID", "Source", "Target", "Starttime", "Endtime",
                        "Planned_Duration", "Real_Duration", "Status", "Count_Subtasks", "Count_Comments");

        final BiConsumer<CSVPrinter, Ticket> printTickets = (csvp, t) -> {
            try {
                csvp.printRecord(t.getName(), t.getUUID(), t.getSource(), t.getTarget(),
                        t.getStarttime(), t.getEndtime(), t.getPlannedDuration(),
                        t.getRealDuration(), t.getStatus(), t.getCountSubtasks(), t.getCountComments());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        return writeCsv(tickets, printTickets, formatWithHeaders, path);
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

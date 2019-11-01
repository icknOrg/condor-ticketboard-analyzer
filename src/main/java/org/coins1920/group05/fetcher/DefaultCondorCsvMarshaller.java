package org.coins1920.group05.fetcher;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.coins1920.group05.fetcher.model.condor.Person;
import org.coins1920.group05.fetcher.model.condor.Ticket;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DefaultCondorCsvMarshaller implements CondorCsvMarshaller {

    private static final String PERSONS_FILENAME = "persons.csv";
    private static final String TICKETS_FILENAME = "tickets.csv";
    private final CSVFormat condorFormat = CSVFormat.DEFAULT;

    @Override
    public void write(List<Person> persons, List<Ticket> tickets, String dir) throws IOException {
        writePersons(persons, dir);
        writeTickets(tickets, dir);
    }

    private void writePersons(List<Person> persons, String dir) throws IOException {
        final Path path = Paths.get(dir, PERSONS_FILENAME);
        final CSVFormat formatWithHeaders = condorFormat
                .withHeader("Id", "Name", "starttime");

        final BufferedWriter writer = Files.newBufferedWriter(path);
        final CSVPrinter csvPrinter = new CSVPrinter(writer, formatWithHeaders);

        persons.stream()
                .filter(Objects::nonNull)
                .forEach(p -> {
                    try {
                        csvPrinter.printRecord(String.valueOf(p.getId()), p.getName(), p.getStarttime());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        csvPrinter.flush();
    }

    private void writeTickets(List<Ticket> tickets, String dir) throws IOException {
        final Path path = Paths.get(dir, TICKETS_FILENAME);
        final CSVFormat formatWithHeaders = condorFormat
                .withHeader("Name", "UUID", "Source", "Target", "Starttime", "Endtime",
                        "Planned_Duration", "Real_Duration", "Status", "Count_Subtasks", "Count_Comments");

        final BufferedWriter writer = Files.newBufferedWriter(path);
        final CSVPrinter csvPrinter = new CSVPrinter(writer, formatWithHeaders);

        tickets.stream()
                .filter(Objects::nonNull)
                .forEach(t -> {
                    try {
                        csvPrinter.printRecord(t.getName(), t.getUUID(), t.getSource(), t.getTarget(),
                                t.getStarttime(), t.getEndtime(), t.getPlannedDuration(),
                                t.getRealDuration(), t.getStatus(), t.getCountSubtasks(), t.getCountComments());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        csvPrinter.flush();
    }

}

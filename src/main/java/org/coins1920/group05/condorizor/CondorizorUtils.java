package org.coins1920.group05.condorizor;

import org.coins1920.group05.fetcher.CondorCsvMarshaller;
import org.coins1920.group05.fetcher.DefaultCondorCsvMarshaller;
import org.coins1920.group05.fetcher.model.condor.Person;
import org.coins1920.group05.fetcher.model.condor.Ticket;
import org.coins1920.group05.fetcher.model.general.AbstractMember;
import org.coins1920.group05.fetcher.model.general.AbstractTicket;
import org.coins1920.group05.fetcher.util.Pair;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class CondorizorUtils {

    public static <M extends AbstractMember, T extends AbstractTicket> Pair<File, File> mapAndWriteToCsvFiles(
            List<M> trelloBoardMembers,
            List<T> trelloCardsForAllAuthors,
            Function<List<M>, List<Person>> toPersons,
            Function<List<T>, List<Ticket>> toTickets, String outputDir) {
        // map to persons and tickets:
        final List<Person> persons = toPersons.apply(trelloBoardMembers);
        final List<Ticket> tickets = toTickets.apply(trelloCardsForAllAuthors);

        // write the CSV files to disc:
        final CondorCsvMarshaller condorCsvMarshaller = new DefaultCondorCsvMarshaller();
        return condorCsvMarshaller.write(persons, tickets, outputDir);
    }
}

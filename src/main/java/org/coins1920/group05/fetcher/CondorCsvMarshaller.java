package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.condor.Person;
import org.coins1920.group05.fetcher.model.condor.Ticket;

import java.io.IOException;
import java.util.List;

public interface CondorCsvMarshaller {
    void write(List<Person> persons, List<Ticket> tickets, String dir) throws IOException;
}

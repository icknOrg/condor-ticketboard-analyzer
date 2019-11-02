package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.condor.Person;
import org.coins1920.group05.fetcher.model.condor.Ticket;
import org.coins1920.group05.fetcher.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface CondorCsvMarshaller {
    Pair<File, File> write(List<Person> persons, List<Ticket> tickets, String dir);
}

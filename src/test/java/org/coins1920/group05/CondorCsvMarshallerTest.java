package org.coins1920.group05;

import org.coins1920.group05.fetcher.CondorCsvMarshaller;
import org.coins1920.group05.fetcher.DefaultCondorCsvMarshaller;
import org.coins1920.group05.fetcher.model.condor.Person;
import org.coins1920.group05.fetcher.model.condor.Ticket;
import org.coins1920.group05.fetcher.util.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for the Condor CSV marshaller.
 */
public class CondorCsvMarshallerTest {

    private Logger logger = LoggerFactory.getLogger(CondorCsvMarshallerTest.class);

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Test writing a simple Condor CSV file.
     */
    @Test
    public void testCsvMarshalling() throws IOException {
        final File testFolder = temporaryFolder.newFolder("test-condor-csv-files");
        final CondorCsvMarshaller condorCsvMarshaller = new DefaultCondorCsvMarshaller();

        final Pair<File, File> csvFiles = condorCsvMarshaller.write(
                testPersons(),
                testTickets(),
                testFolder.getAbsolutePath()
        );
        assertThat(csvFiles, is(not(nullValue())));
        assertThat(csvFiles.getFirst(), is(not(nullValue())));
        assertThat(csvFiles.getSecond(), is(not(nullValue())));
    }

    private List<Person> testPersons() {
        final List<Person> persons = new LinkedList<>();
        persons.add(new Person(1L, "ralf", "2010-09-12T04:00:00+00:00"));
        persons.add(new Person(2L, "mike", "2010-09-12T04:00:00+00:00"));
        persons.add(new Person(3L, "meike", "2010-09-12T04:00:00+00:00"));
        persons.add(new Person(4L, "anna", "2010-09-12T04:00:00+00:00"));
        return persons;
    }

    private List<Ticket> testTickets() {
        final List<Ticket> tickets = new LinkedList<>();

        tickets.add(new Ticket("Ticket 13", "1312", "1", "2",
                "2012-09-12T04:00:00+00:00", "2012-09-20T04:00:00+00:00", "15",
                "8", "closed", "3", "7"));

        tickets.add(new Ticket("Ticket 13", "1313", "1", "3",
                "2012-09-12T04:00:00+00:00", "2012-09-20T04:00:00+00:00", "15",
                "8", "closed", "3", "7"));

        tickets.add(new Ticket("Ticket 15", "1521", "2", "4",
                "2012-10-12T04:00:00+00:00", "2012-10-20T04:00:00+00:00", "15",
                "8", "closed", "1", "0"));
        return tickets;
    }

}

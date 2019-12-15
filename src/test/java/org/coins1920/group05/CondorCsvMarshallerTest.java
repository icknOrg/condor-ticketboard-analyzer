package org.coins1920.group05;

import org.coins1920.group05.condorizor.CondorCsvMarshaller;
import org.coins1920.group05.condorizor.DefaultCondorCsvMarshaller;
import org.coins1920.group05.model.condor.Actor;
import org.coins1920.group05.model.condor.Edge;
import org.coins1920.group05.model.condor.EdgeType;
import org.coins1920.group05.util.Pair;
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
                testActors(),
                testEdges(),
                testFolder.getAbsolutePath()
        );
        assertThat(csvFiles, is(not(nullValue())));
        assertThat(csvFiles.getFirst(), is(not(nullValue())));
        assertThat(csvFiles.getSecond(), is(not(nullValue())));
    }

    private List<Actor> testActors() {
        final List<Actor> actors = new LinkedList<>();
        actors.add(new Actor("1", "ralf", "2010-09-12T04:00:00+00:00",
                "", "", "", null));
        actors.add(new Actor("2", "mike", "2010-09-12T04:00:00+00:00",
                "", "", "", null));
        actors.add(new Actor("3", "meike", "2010-09-12T04:00:00+00:00",
                "", "", "", null));
        actors.add(new Actor("4", "anna", "2010-09-12T04:00:00+00:00",
                "", "", "", null));
        return actors;
    }

    private List<Edge> testEdges() {
        final List<Edge> edges = new LinkedList<>();

        edges.add(new Edge("Ticket 13", "1312", "1", "2",
                "2012-09-12T04:00:00+00:00", "2012-09-20T04:00:00+00:00", "15",
                "8", "closed", "3", "7", "",
                EdgeType.CREATION));

        edges.add(new Edge("Ticket 13", "1313", "1", "3",
                "2012-09-12T04:00:00+00:00", "2012-09-20T04:00:00+00:00", "15",
                "8", "closed", "3", "7", "",
                EdgeType.COMMENT));

        edges.add(new Edge("Ticket 15", "1521", "2", "4",
                "2012-10-12T04:00:00+00:00", "2012-10-20T04:00:00+00:00", "15",
                "8", "closed", "1", "0", "",
                EdgeType.REACTION));

        return edges;
    }

}

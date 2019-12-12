package org.coins1920.group05.condorizor;

import org.coins1920.group05.model.condor.Actor;
import org.coins1920.group05.model.condor.Edge;
import org.coins1920.group05.util.Pair;

import java.io.File;
import java.util.List;

public interface CondorCsvMarshaller {
    Pair<File, File> write(List<Actor> actors, List<Edge> edges, String dir);
}

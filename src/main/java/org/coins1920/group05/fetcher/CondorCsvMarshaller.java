package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.condor.Actor;
import org.coins1920.group05.fetcher.model.condor.Edge;
import org.coins1920.group05.fetcher.util.Pair;

import java.io.File;
import java.util.List;

public interface CondorCsvMarshaller {
    Pair<File, File> write(List<Actor> actors, List<Edge> edges, String dir);
}

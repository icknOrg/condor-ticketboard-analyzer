package org.coins1920.group05.fetcher.model.condor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Edge {
    private String Name;
    private String UUID;
    private String Source;
    private String Target;
    private String Starttime;
    private String Endtime;
    private String PlannedDuration;
    private String RealDuration;
    private String Status;
    private String CountSubtasks;
    private String CountComments;
    private EdgeType edgeType;
}

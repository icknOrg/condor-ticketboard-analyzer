package org.coins1920.group05.model.condor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Edge {
    private String name;
    private String uuid;
    private String source;
    private String target;
    private String startTime;
    private String endTime;
    private String plannedDuration;
    private String realDuration;
    private String status;
    private String countSubtasks;
    private String countComments;
    private String commentBody;
    private EdgeType edgeType;
}

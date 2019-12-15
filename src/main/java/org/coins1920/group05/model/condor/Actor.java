package org.coins1920.group05.model.condor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Actor {
    private String id;
    private String name;
    private String starttime;
    private String company;
    private String location;
    private String email;
    private Boolean hireable;
}

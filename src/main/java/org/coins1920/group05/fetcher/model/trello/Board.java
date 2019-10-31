package org.coins1920.group05.fetcher.model.trello;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Board {
    private String id;
    private String name;
    private String desc;
    private String descData;
    private boolean closed;
    private String idOrganization;
    private boolean pinned;
    private String url;
    private String shortUrl;
//    private String[] prefs; // TODO: model the object
//    private String[] labelNames; // TODO: model the object
    private boolean starred;
//    private String[] limits; // TODO: model the object
//    private String[] memberships;
    private boolean enterpriseOwned;
}

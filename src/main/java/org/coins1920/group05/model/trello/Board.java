package org.coins1920.group05.model.trello;

import lombok.Getter;
import lombok.ToString;
import org.coins1920.group05.model.general.AbstractBoard;

@Getter
@ToString(callSuper = true)
public class Board extends AbstractBoard {
    private String name;
    private String desc;
    private String descData;
    private boolean closed;
    private String idOrganization;
    private boolean pinned;
    private String url;
    private String shortUrl;
    private boolean starred;
    private boolean enterpriseOwned;
}

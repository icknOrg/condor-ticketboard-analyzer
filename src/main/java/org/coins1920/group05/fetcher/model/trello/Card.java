package org.coins1920.group05.fetcher.model.trello;

import lombok.Getter;
import lombok.ToString;
import org.coins1920.group05.fetcher.model.general.AbstractTicket;

@Getter
@ToString(callSuper = true)
public class Card extends AbstractTicket {
    private String name;
    private String url;
    private String closed;
//    private String author; // is NOT part of the original API object!
}

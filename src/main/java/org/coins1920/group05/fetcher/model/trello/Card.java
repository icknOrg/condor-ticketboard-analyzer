package org.coins1920.group05.fetcher.model.trello;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.coins1920.group05.fetcher.model.general.AbstractTicket;

@Getter
@Setter
@ToString(callSuper = true)
public class Card extends AbstractTicket {
    private String name;
    private String url;
    private String closed;
    private String creator; // the author that created the card - is NOT part of the original API object!
    private String author; // someone tho wrote a comment or changed the card - is NOT part of the original API object!
}

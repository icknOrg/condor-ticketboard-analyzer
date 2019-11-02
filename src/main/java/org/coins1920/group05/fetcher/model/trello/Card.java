package org.coins1920.group05.fetcher.model.trello;

import lombok.Getter;
import org.coins1920.group05.fetcher.model.general.AbstractTicket;

@Getter
public class Card extends AbstractTicket {
    private String url;
    private String closed;

}

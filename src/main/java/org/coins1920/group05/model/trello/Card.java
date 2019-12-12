package org.coins1920.group05.fetcher.model.trello;

import lombok.*;
import org.coins1920.group05.fetcher.model.general.AbstractTicket;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Card extends AbstractTicket implements Serializable {
    private String name;
    private String url;
    private String closed;
    private Member[] members;
    private String creator; // the author that created the card - is NOT part of the original API object!
    private String author; // someone tho wrote a comment or changed the card - is NOT part of the original API object!
}

package org.coins1920.group05.fetcher.model.github;

import lombok.*;
import org.coins1920.group05.fetcher.model.general.AbstractTicket;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Issue extends AbstractTicket {
    private String number;
    private String title;
    private String state;
    private String created_at;
}

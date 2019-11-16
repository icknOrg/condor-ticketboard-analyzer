package org.coins1920.group05.fetcher.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String url;

    @JsonProperty("comments_url")
    private String commentsUrl; // points to all comments linked to this issue

    @JsonProperty("events_url")
    private String eventsUrl; // points to all events linked to this issue

    private String state;
    private String created_at;
    private User[] assignees;
    private User user; // the ticket author
}

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
    private User user; // the ticket author

    @JsonProperty("comments_url")
    private String commentsUrl; // points to all comments linked to this issue

    @JsonProperty("events_url")
    private String eventsUrl; // points to all events linked to this issue

    private String state;
    private String comments; // the number of comments attached to this issue
    private User[] assignees;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("closed_at")
    private String closedAt;

    @JsonProperty("pull_request")
    private PullRequest pullRequest;
}

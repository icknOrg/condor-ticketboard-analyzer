package org.coins1920.group05.fetcher.model.trello;

import lombok.Data;

@Data
public class Member {
    private String id;
    private String avatarHash;
    private String avatarUrl;
    private String initials;
    private String fullName;
    private String username;
    private String confirmed;
    private String memberType;
}

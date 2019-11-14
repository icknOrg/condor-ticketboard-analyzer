package org.coins1920.group05.fetcher.model.github;

import lombok.Getter;

@Getter
public class Comment {
    private String id;
    private User user; // the author of this very comment
    private String created_at;
    private String updated_at;
}

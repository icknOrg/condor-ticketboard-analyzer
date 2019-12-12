package org.coins1920.group05.fetcher.model.github;

import lombok.*;
import org.coins1920.group05.fetcher.model.general.AbstractComment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Comment extends AbstractComment {
    private User user; // the author of this very comment
    private String created_at;
    private String updated_at;
    private String body;
}

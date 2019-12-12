package org.coins1920.group05.fetcher.model.github;

import lombok.Getter;
import lombok.ToString;
import org.coins1920.group05.fetcher.model.general.AbstractMember;

@Getter
@ToString(callSuper = true)
public class User extends AbstractMember {
    private String login;
    private String contributions;
}

package org.coins1920.group05.model.github.rest;

import lombok.Getter;
import lombok.ToString;
import org.coins1920.group05.model.general.AbstractMember;

@Getter
@ToString(callSuper = true)
public class User extends AbstractMember {
    private String login; // the user name
    private String name;
    private String company;
    private String location;
    private String email;
    private boolean hireable;
    private String url; // URL to the user in GitHub's API
}

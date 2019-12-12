package org.coins1920.group05.model.trello;

import lombok.Getter;
import lombok.ToString;
import org.coins1920.group05.model.general.AbstractAction;

@Getter
@ToString(callSuper = true)
public class Action extends AbstractAction {
    private String type;
    private Member memberCreator;
}

package org.coins1920.group05.model.general;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * A simple product type that holds a collection for each user type.
 *
 * @param <U> the type parameter
 */
@Getter
@AllArgsConstructor
public class CategorizedBoardMembers<U extends AbstractMember> {
    private U creator;
    private List<U> assignees;
    private List<U> commentators;
}

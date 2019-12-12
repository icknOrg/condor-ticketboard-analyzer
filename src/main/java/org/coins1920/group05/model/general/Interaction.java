package org.coins1920.group05.model.general;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.coins1920.group05.model.condor.EdgeType;

/**
 * A generic interaction "sum type": creation | comment .
 */
@Getter
@AllArgsConstructor
public class Interaction<M extends AbstractMember, C extends AbstractComment> {
    private M creator;
    private C comment;
    private EdgeType edgeType;
}

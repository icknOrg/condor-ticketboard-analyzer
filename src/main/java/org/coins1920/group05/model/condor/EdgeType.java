package org.coins1920.group05.model.condor;

public enum EdgeType {
    COMMENT("COMMENT"),
    ASSIGNING("ASSIGNING"),
    CREATION("CREATION"),
    REACTION("REACTION"); // a mere reaction, e.g. adding an emoiji-like

    public final String label;

    EdgeType(String label) {
        this.label = label;
    }
}

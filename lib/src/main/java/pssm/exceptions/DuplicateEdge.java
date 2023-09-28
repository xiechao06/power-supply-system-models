package pssm.exceptions;

import org.apache.commons.math3.util.Pair;

import pssm.Utils;

public class DuplicateEdge extends IllegalArgumentException {
    public final String firstPortId;
    public final String secondPortId;
    Object extras;

    public DuplicateEdge(String firstPortId, String secondPortId, Object extras) {
        super("Duplicate Edge: from " + firstPortId + " to " + secondPortId + " extras - "
                + extras);
        this.firstPortId = firstPortId;
        this.secondPortId = secondPortId;
        this.extras = extras;
    }

    public DuplicateEdge(
            Pair<String, Integer> first,
            Pair<String, Integer> second,
            Object extras) {
        this(Utils.genPortId(first), Utils.genPortId(second), extras);
    }
}
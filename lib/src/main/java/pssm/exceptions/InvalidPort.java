package pssm.exceptions;

import org.apache.commons.math3.util.Pair;

import pssm.Utils;

public class InvalidPort extends IllegalArgumentException {
    public final String portId;

    public InvalidPort(Pair<String, Integer> port) {
        this(Utils.genPortId(port));
    }

    public InvalidPort(String portId) {
        super("Invalid port: " + portId);
        this.portId = portId;
    }
}
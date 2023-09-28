package pssm;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.math3.util.Pair;

public final class PowerSupplySystemTree {
    private final DirectedPort root;

    public DirectedPort getRoot() {
        return root;
    }

    private final Map<String, DirectedPort> nodes;

    public Map<String, DirectedPort> getNodes() {
        return nodes;
    }

    public PowerSupplySystemTree(DirectedPort root, Map<String, DirectedPort> nodes) {
        this.root = root;
        this.nodes = nodes;
    }

    /**
     * Finds the passage of a given port in the power supply system tree.
     * 
     * @param portArg the port to find the passage for, represented as a pair of
     *                port name and index
     * @return the passage of the given port, represented as a list of pairs of
     *         device name and port index
     */
    public Passage findPassage(Pair<String, Integer> portArg) {
        DirectedPort port = nodes.get(Utils.genPortId(portArg));
        if (port == null) {
            return null;
        }
        Passage passage = new Passage();
        do {
            passage.add(Pair.create(port.device.name, port.portIndex));
            port = port.getParent();
        } while (port != null);
        Collections.reverse(passage);
        return passage;
    }
}
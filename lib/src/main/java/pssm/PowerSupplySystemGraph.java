package pssm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;

import pssm.devices.BaseDevice;
import pssm.devices.DcDc;
import pssm.devices.Diode;
import pssm.devices.PowerSupply;
import pssm.devices.Switch;
import pssm.exceptions.ChargePowerSupply;
import pssm.exceptions.DuplicateDevice;
import pssm.exceptions.DuplicateEdge;
import pssm.exceptions.InvalidPort;
import pssm.exceptions.LackPowerSupplies;
import pssm.exceptions.NoSuchDevice;

final public class PowerSupplySystemGraph {
    private Map<String, Port> ports;

    public Map<String, Port> getPorts() {
        return ports;
    }

    private Map<String, BaseDevice> devices;

    public Map<String, BaseDevice> getDevices() {
        return devices;
    }

    private List<Edge> edges;

    public List<Edge> getEdges() {
        return edges;
    }

    public PowerSupplySystemGraph() {
        ports = new HashMap<>();
        devices = new HashMap<>();
        edges = new ArrayList<>();
    }

    /**
     * Adds a device to the power supply system graph.
     * 
     * @param device the device to be added
     * @throws DuplicateDevice if a device with the same name already exists in the
     *                         graph
     */
    public <T extends BaseDevice> void addDevice(T device) throws DuplicateDevice {

        if (devices.containsKey(device.name)) {
            throw new DuplicateDevice(device.name);
        }
        devices.put(device.name, device);
        for (int i = 0; i < device.getNumPorts(); i++) {
            Port port = new Port(device, i);
            ports.put(port.getId(), port);
        }
    }

    public void addEdge(Pair<String, Integer> first, Pair<String, Integer> second) {
        addEdge(first, second, null);
    }

    public void addEdge(Pair<String, Integer> first, Pair<String, Integer> second, Object extras) {
        addEdge(Utils.genPortId(first), Utils.genPortId(second), extras);
    }

    public void addEdge(String firstPortId, String secondPortId) {
        addEdge(firstPortId, secondPortId, null);
    }

    public void addEdge(String firstPortId, String secondPortId, Object extras)
            throws InvalidPort, DuplicateEdge {
        if (!ports.containsKey(firstPortId)) {
            throw new InvalidPort(firstPortId);
        }
        if (!ports.containsKey(secondPortId)) {
            throw new InvalidPort(secondPortId);
        }
        Port firstPort = ports.get(firstPortId);
        Port secondPort = ports.get(secondPortId);

        for (Pair<Port, Object> neighbour : firstPort.adjList) {
            if (neighbour.getFirst().getId().equals(secondPortId)) {
                throw new DuplicateEdge(firstPortId, secondPortId, extras);
            }
        }
        Edge edge = new Edge(firstPort, secondPort, extras);
        edges.add(edge);

        firstPort.adjList.add(new Pair<>(secondPort, extras));
        secondPort.adjList.add(new Pair<>(firstPort, extras));

    }

    public List<PowerSupplySystemTree> genForest() {
        return genForest(null);
    }

    /**
     * Generates a forest of power supply system trees based on the given truth
     * table.
     * If the truth table is null, an empty truth table is used.
     * 
     * @param truthTableArg the truth table to use for generating the forest
     * @return a list of power supply system trees representing the forest
     * @throws NoSuchDevice             if a device in the truth table does not
     *                                  exist in the system
     * @throws IllegalArgumentException if a device in the truth table is not a
     *                                  switch
     * @throws LackPowerSupplies        if there are no power supplies in the system
     * @throws ChargePowerSupply        if a power supply is connected to another
     *                                  power supply
     */
    public List<PowerSupplySystemTree> genForest(Map<String, Boolean> truthTableArg)
            throws NoSuchDevice, IllegalArgumentException, LackPowerSupplies, ChargePowerSupply {
        Map<String, Boolean> truthTable = truthTableArg == null ? new HashMap<>() : truthTableArg;
        // check truth table
        for (String key : truthTable.keySet()) {
            if (!this.devices.containsKey(key)) {
                throw new NoSuchDevice(key);
            }
            BaseDevice device = this.devices.get(key);
            if (!(device instanceof Switch)) {
                throw new IllegalArgumentException(key + " is not a switch");
            }
        }

        List<DirectedPort> directedRoots = new ArrayList<>();

        for (Port port : this.ports.values()) {
            if (port.device instanceof PowerSupply) {
                DirectedPort directedRoot = new DirectedPort(port.device, port.index);
                directedRoots.add(directedRoot);
            }
        }

        if (directedRoots.isEmpty()) {
            throw new LackPowerSupplies();
        }

        List<PowerSupplySystemTree> forest = new ArrayList<>();

        for (DirectedPort directedRoot : directedRoots) {
            assert directedRoot.device instanceof PowerSupply;
            // list of candidates and their parent
            final List<Pair<DirectedPort, DirectedPort>> stack = new LinkedList<>();
            // power supply is the root, so it has no parent
            stack.add(Pair.create(directedRoot, null));
            Map<String, DirectedPort> visited = new HashMap<>();

            while (!stack.isEmpty()) {
                Pair<DirectedPort, DirectedPort> pair = stack.remove(0);
                DirectedPort candidate = pair.getFirst();
                visited.put(candidate.getId(), candidate);
                DirectedPort parent = pair.getSecond();
                // NOTE: 注意一定要克隆出一个新的列表, 因为要修改该列表
                List<Pair<Port, Object>> adjList = new ArrayList<>(ports.get(candidate.getId()).adjList);
                BaseDevice candidateDevice = candidate.device;
                boolean isClosedSwitch = (candidateDevice instanceof Switch)
                        && truthTable.getOrDefault(candidateDevice.name,
                                ((Switch) candidateDevice).isClosed());
                boolean isDcDc = candidateDevice instanceof DcDc;
                boolean isDiodePort0 = candidateDevice instanceof Diode && candidate.portIndex == 0;

                if (isClosedSwitch || isDcDc || isDiodePort0) {
                    Port theOtherPort = ports.get(Utils.genPortId(candidateDevice.name, 1 - candidate.portIndex));
                    // 这个边是模拟出来的，所以没有extras
                    adjList.add(Pair.create(theOtherPort, null));
                }

                for (Pair<Port, Object> neighbour : adjList) {
                    Port childPort = neighbour.getFirst();
                    Object extras = neighbour.getSecond();
                    // 避免回溯到父节点
                    if (parent != null && childPort.getId().equals(parent.getId())) {
                        continue;
                    }
                    if (childPort.device instanceof PowerSupply) {
                        throw new ChargePowerSupply(directedRoot.device, childPort.device);
                    }
                    DirectedPort directedChildPort = new DirectedPort(childPort.device, childPort.index);
                    candidate.children.add(directedChildPort);
                    directedChildPort.setParent(candidate);
                    DirectedEdge directedEdge = new DirectedEdge(candidate, directedChildPort, extras);
                    candidate.edges.add(directedEdge);

                    stack.add(Pair.create(directedChildPort, candidate));
                }
            }

            PowerSupplySystemTree tree = new PowerSupplySystemTree(directedRoot, visited);
            forest.add(tree);
        }

        return forest;
    }

    public Map<String, List<Passage>> findPassages(List<Pair<String, Integer>> destinations) {
        return findPassages(destinations, null);
    }

    /**
     * Finds passages start from power supplies for the given destinations and truth
     * table arguments.
     * 
     * @param destinations  A list of pairs containing the ID of the destination
     *                      port and the required voltage level.
     * @param truthTableArg A map containing the truth table arguments.
     * @return A map containing the ID of the destination port and a list of
     *         passages to reach that port.
     * @throws NoSuchDevice             if a device in the truth table does not
     * @throws IllegalArgumentException if a device in the truth table is not a
     * @throws LackPowerSupplies        if there are no power supplies in the system
     * @throws ChargePowerSupply        if a power supply is connected to another
     */
    public Map<String, List<Passage>> findPassages(List<Pair<String, Integer>> destinations,
            Map<String, Boolean> truthTableArg)
            throws NoSuchDevice, IllegalArgumentException, LackPowerSupplies, ChargePowerSupply {
        Map<String, List<Passage>> res = new HashMap<>();
        List<PowerSupplySystemTree> forest = genForest(truthTableArg);
        for (Pair<String, Integer> port : destinations) {
            String portId = Utils.genPortId(port);
            for (PowerSupplySystemTree tree : forest) {
                Passage passage = tree.findPassage(port);
                if (passage != null) {
                    if (!res.containsKey(portId)) {
                        res.put(portId, new ArrayList<>());
                    }
                    res.get(portId).add(passage);
                }
            }
        }
        return res;
    }
}

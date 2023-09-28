package pssm;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static pssm.Utils.genPortId;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pssm.devices.Bus;
import pssm.devices.DcDc;
import pssm.devices.Diode;
import pssm.devices.Load;
import pssm.devices.PowerSupply;
import pssm.devices.Switch;
import pssm.exceptions.ChargePowerSupply;
import pssm.exceptions.InvalidPort;
import pssm.exceptions.LackPowerSupplies;
import pssm.exceptions.NoSuchDevice;

public class PowerSupplySystemGraphTest {

    private PowerSupplySystemGraph graph;

    @BeforeClass
    public void setUp() {
        graph = new PowerSupplySystemGraph();

        graph.addDevice(new PowerSupply("power_supply_0"));
        graph.addDevice(new PowerSupply("power_supply_1"));

        graph.addDevice(new Switch("switch_0"));
        graph.addEdge("power_supply_0.0", "switch_0.0");
        graph.addDevice(new Switch("switch_1"));
        graph.addEdge("power_supply_1.0", "switch_1.0");

        graph.addDevice(new Bus("bus_0"));
        graph.addDevice(new Bus("bus_1"));
        graph.addEdge("switch_0.1", "bus_0.0");
        graph.addEdge("switch_1.1", "bus_1.0");

        graph.addDevice(new Switch("switch_2"));
        graph.addEdge("bus_0.0", "switch_2.0");
        graph.addEdge("bus_1.0", "switch_2.1");

        graph.addDevice(new Load("load_0"));
        graph.addDevice(new Load("load_1"));
        graph.addEdge("bus_0.0", "load_0.0");
        graph.addEdge("bus_1.0", "load_1.0");
    }

    @Test
    public void addPowerSupply() {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        PowerSupply powerSupply = new PowerSupply("power_supply_0");
        graph.addDevice(powerSupply);
        assertEquals(graph.getDevices().size(), 1);
        assertTrue(graph.getDevices().containsKey("power_supply_0"));
        assertEquals(graph.getPorts().size(), 1);
        assertTrue(graph.getEdges().isEmpty());
        assertTrue(graph.getPorts().containsKey(genPortId(powerSupply.name, 0)));
    }

    @Test(expectedExceptions = {
            pssm.exceptions.DuplicateDevice.class }, expectedExceptionsMessageRegExp = "Duplicate device: power_supply_0")
    public void addPowerSupplyTwice() throws pssm.exceptions.DuplicateDevice {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        graph.addDevice(new PowerSupply("power_supply_0"));
        graph.addDevice(new PowerSupply("power_supply_0"));
    }

    @Test
    public void addEdge() {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        graph.addDevice(new PowerSupply("power_supply_0"));
        graph.addDevice(new PowerSupply("power_supply_1"));

        try {
            graph.addEdge(Pair.create("power_supply_0", 1), Pair.create("power_supply_1", 0));
        } catch (InvalidPort e) {
            assertEquals(e.portId, "power_supply_0.1");
        }

        try {
            graph.addEdge(Pair.create("power_supply_0", 0), Pair.create("power_supply_1", 1));
        } catch (InvalidPort e) {
            assertEquals(e.portId, "power_supply_1.1");
        }
        graph.addEdge(Pair.create("power_supply_0", 0), Pair.create("power_supply_1", 0));

        assertEquals(graph.getEdges().size(), 1);
        Edge edge = graph.getEdges().get(0);
        assertEquals(edge.first.getId(), genPortId("power_supply_0", 0));
        assertEquals(edge.second.getId(), genPortId("power_supply_1", 0));

        assertEquals(graph.getPorts().size(), 2);
        Port first = graph.getPorts().get(genPortId("power_supply_0", 0));
        Port second = graph.getPorts().get(genPortId("power_supply_1", 0));

        assertEquals(first.adjList.size(), 1);
        assertEquals(second.adjList.size(), 1);

        assertEquals(first.adjList.get(0).getFirst(), second);
        assertEquals(second.adjList.get(0).getFirst(), first);
    }

    @Test
    public void genForestWithInvalidTruthTable() {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        try {
            graph.genForest(new HashMap<>() {
                {
                    put("switch_0", true);
                }
            });
        } catch (NoSuchDevice e) {
            assertEquals(e.deviceName, "switch_0");
        }

        graph.addDevice(new PowerSupply("switch_0"));

        try {
            graph.genForest(new HashMap<>() {
                {
                    put("switch_0", true);
                }
            });
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "switch_0 is not a switch");
        }

        try {
            graph.genForest();
        } catch (LackPowerSupplies e) {

        }
    }

    @Test
    public void genForest1() {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        PowerSupply powerSupply = new PowerSupply("power_supply");
        graph.addDevice(powerSupply);
        List<PowerSupplySystemTree> forest = graph.genForest();
        assertEquals(forest.size(), 1);

        PowerSupplySystemTree tree = forest.get(0);
        DirectedPort root = tree.getRoot();

        assert root.device == powerSupply;
        assert root.portIndex == 0;
        assert root.children.isEmpty();
    }

    @Test
    public void genForest2() {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        PowerSupply powerSupply0 = new PowerSupply("power_supply_0");
        graph.addDevice(powerSupply0);

        PowerSupply powerSupply1 = new PowerSupply("power_supply_1");
        graph.addDevice(powerSupply1);
        List<PowerSupplySystemTree> forest = graph.genForest();
        assertEquals(forest.size(), 2);

        PowerSupplySystemTree tree0 = forest.get(0);
        PowerSupplySystemTree tree1 = forest.get(1);
        DirectedPort _0 = tree0.getRoot();

        assert _0.device == powerSupply0;
        assert _0.portIndex == 0;
        assert _0.children.isEmpty();

        DirectedPort _1 = tree1.getRoot();
        assert _1.device == powerSupply1;
        assert _1.portIndex == 0;
        assert _0.children.isEmpty();
    }

    @Test
    public void genForest3() {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        PowerSupply powerSupply0 = new PowerSupply("power_supply_0");
        graph.addDevice(powerSupply0);

        PowerSupply powerSupply1 = new PowerSupply("power_supply_1");
        graph.addDevice(powerSupply1);

        graph.addEdge(Pair.create("power_supply_0", 0), Pair.create("power_supply_1", 0));

        try {
            graph.genForest();
            throw new RuntimeException("shouln't reach here");
        } catch (ChargePowerSupply e) {
            assertEquals(e.from, powerSupply0);
            assertEquals(e.to, powerSupply1);
        }
    }

    @Test
    public void getForest4() {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        PowerSupply powerSupply0 = new PowerSupply("power_supply_0");
        graph.addDevice(powerSupply0);

        Switch switch_ = new Switch("switch");
        graph.addDevice(switch_);

        PowerSupply powerSupply1 = new PowerSupply("power_supply_1");
        graph.addDevice(powerSupply1);

        graph.addEdge(Pair.create("power_supply_0", 0), Pair.create("switch", 0));
        graph.addEdge(Pair.create("switch", 1), Pair.create("power_supply_1", 0));

        switch_.close();

        try {
            graph.genForest();
            throw new RuntimeException("shouln't reach here");
        } catch (ChargePowerSupply e) {
            assertEquals(e.from, powerSupply0);
            assertEquals(e.to, powerSupply1);
        }

        switch_.open();

        List<PowerSupplySystemTree> forest = graph.genForest();
        assertEquals(forest.size(), 2);

        PowerSupplySystemTree tree0 = forest.get(0);
        assertEquals(tree0.getNodes().size(), 2);
        assertTrue(tree0.getNodes().containsKey("power_supply_0.0"));
        assertTrue(tree0.getNodes().containsKey("switch.0"));

        DirectedPort _0 = tree0.getRoot();
        assertEquals(_0.device, powerSupply0);
        assertEquals(_0.portIndex, 0);
        assertEquals(_0.children.size(), 1);

        DirectedPort _00 = _0.children.get(0);
        assertEquals(_00.device, switch_);
        assertEquals(_00.portIndex, 0);
        assertTrue(_00.children.isEmpty());

        PowerSupplySystemTree tree1 = forest.get(1);
        assertEquals(tree1.getNodes().size(), 2);
        assertTrue(tree1.getNodes().containsKey("power_supply_1.0"));
        assertTrue(tree1.getNodes().containsKey("switch.1"));

        DirectedPort _1 = tree1.getRoot();
        assertEquals(_1.device, powerSupply1);
        assertEquals(_1.portIndex, 0);
        assertEquals(_1.children.size(), 1);

        DirectedPort _10 = _1.children.get(0);
        assertEquals(_10.device, switch_);
        assertEquals(_10.portIndex, 1);
        assertTrue(_10.children.isEmpty());
    }

    @Test
    public void genForest5() {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        PowerSupply powerSupply0 = new PowerSupply("power_supply_0");
        graph.addDevice(powerSupply0);

        Diode diode0 = new Diode("diode_0");
        graph.addDevice(diode0);
        graph.addEdge("power_supply_0.0", "diode_0.0");

        PowerSupply powerSupply1 = new PowerSupply("power_supply_1");
        graph.addDevice(powerSupply1);

        Diode diode1 = new Diode("diode_1");
        graph.addDevice(diode1);
        graph.addEdge("power_supply_1.0", "diode_1.0");

        Bus bus = new Bus("bus");
        graph.addDevice(bus);
        graph.addEdge("diode_0.1", "bus.0");
        graph.addEdge("diode_1.1", "bus.0");

        Load load = new Load("load");
        graph.addDevice(load);
        graph.addEdge("bus.0", "load.0");

        List<PowerSupplySystemTree> forest = graph.genForest();
        assertEquals(forest.size(), 2);

        PowerSupplySystemTree tree0 = forest.get(0);
        assertEquals(tree0.getNodes().size(), 6);

        assertEquals(tree0.getNodes().keySet(), new HashSet<String>() {
            {
                add("power_supply_0.0");
                add("diode_0.0");
                add("diode_0.1");
                add("bus.0");
                add("diode_1.1");
                add("load.0");
            }
        });

        DirectedPort _0 = tree0.getRoot();
        assertEquals(_0.device, powerSupply0);
        assertEquals(_0.portIndex, 0);
        assertEquals(_0.children.size(), 1);

        DirectedPort _00 = _0.children.get(0);
        assertEquals(_00.device, diode0);
        assertEquals(_00.portIndex, 0);
        assertEquals(_00.children.size(), 1);

        DirectedPort _000 = _00.children.get(0);
        assertEquals(_000.device, diode0);
        assertEquals(_000.portIndex, 1);
        assertEquals(_000.children.size(), 1);

        DirectedPort _0000 = _000.children.get(0);
        assertEquals(_0000.device, bus);
        assertEquals(_0000.portIndex, 0);
        assertEquals(_0000.children.size(), 2);

        DirectedPort _00000 = _0000.children.get(0);
        assertEquals(_00000.device, diode1);
        assertEquals(_00000.portIndex, 1);
        assertTrue(_00000.children.isEmpty());

        DirectedPort _00001 = _0000.children.get(1);
        assertEquals(_00001.device, load);
        assertEquals(_00001.portIndex, 0);
        assertTrue(_00001.children.isEmpty());

        PowerSupplySystemTree tree1 = forest.get(1);
        assertEquals(tree1.getNodes().size(), 6);
        assertEquals(tree1.getNodes().keySet(), new HashSet<String>() {
            {
                add("power_supply_1.0");
                add("diode_1.0");
                add("diode_1.1");
                add("bus.0");
                add("diode_0.1");
                add("load.0");
            }
        });

        DirectedPort _1 = tree1.getRoot();
        assertEquals(_1.device, powerSupply1);
        assertEquals(_1.portIndex, 0);
        assertEquals(_1.children.size(), 1);

        DirectedPort _10 = _1.children.get(0);
        assertEquals(_10.device, diode1);
        assertEquals(_10.portIndex, 0);
        assertEquals(_10.children.size(), 1);

        DirectedPort _100 = _10.children.get(0);
        assertEquals(_100.device, diode1);
        assertEquals(_100.portIndex, 1);
        assertEquals(_100.children.size(), 1);

        DirectedPort _1000 = _100.children.get(0);
        assertEquals(_1000.device, bus);
        assertEquals(_1000.portIndex, 0);
        assertEquals(_1000.children.size(), 2);

        DirectedPort _10000 = _1000.children.get(0);
        assertEquals(_10000.device, diode0);
        assertEquals(_10000.portIndex, 1);
        assertTrue(_10000.children.isEmpty());

        DirectedPort _10001 = _1000.children.get(1);
        assertEquals(_10001.device, load);
        assertEquals(_10001.portIndex, 0);
        assertTrue(_10001.children.isEmpty());
    }

    @Test
    public void genForest6() {
        try {
            graph.genForest(new HashMap<>() {
                {
                    put("switch_0", true);
                    put("switch_1", true);
                    put("switch_2", true);
                }
            });
            throw new RuntimeException("shoulnd't reach here");
        } catch (ChargePowerSupply e) {
            assertEquals(e.from, graph.getDevices().get("power_supply_0"));
            assertEquals(e.to, graph.getDevices().get("power_supply_1"));
        }
    }

    @Test
    public void genForest7() {
        List<PowerSupplySystemTree> forest = graph.genForest(new HashMap<>() {
            {
                put("switch_0", true);
                put("switch_1", true);
                put("switch_2", false);
            }
        });

        assertEquals(forest.size(), 2);

        PowerSupplySystemTree tree0 = forest.get(0);
        assertEquals(tree0.getNodes().size(), 6);

        assertEquals(tree0.getNodes().keySet(), new HashSet<String>() {
            {
                add("power_supply_0.0");
                add("switch_0.0");
                add("switch_0.1");
                add("bus_0.0");
                add("switch_2.0");
                add("load_0.0");
            }
        });

        DirectedPort _0 = tree0.getRoot();
        assertEquals(_0.getId(), "power_supply_0.0");
        assertEquals(_0.children.size(), 1);

        DirectedPort _00 = _0.children.get(0);
        assertEquals(_00.getId(), "switch_0.0");
        assertEquals(_00.children.size(), 1);

        DirectedPort _000 = _00.children.get(0);
        assertEquals(_000.getId(), "switch_0.1");
        assertEquals(_000.children.size(), 1);

        DirectedPort _0000 = _000.children.get(0);
        assertEquals(_0000.getId(), "bus_0.0");
        assertEquals(_0000.children.size(), 2);

        DirectedPort _00000 = _0000.children.get(0);
        assertEquals(_00000.getId(), "switch_2.0");
        assertTrue(_00000.children.isEmpty());

        DirectedPort _00001 = _0000.children.get(1);
        assertEquals(_00001.getId(), "load_0.0");
        assertTrue(_00001.children.isEmpty());

        PowerSupplySystemTree tree1 = forest.get(1);

        assertEquals(tree1.getNodes().size(), 6);
        assertEquals(tree1.getNodes().keySet(), new HashSet<String>() {
            {
                add("power_supply_1.0");
                add("switch_1.0");
                add("switch_1.1");
                add("bus_1.0");
                add("switch_2.1");
                add("load_1.0");
            }
        });

        DirectedPort _1 = tree1.getRoot();
        assertEquals(_1.getId(), "power_supply_1.0");
        assertEquals(_1.children.size(), 1);

        DirectedPort _10 = _1.children.get(0);
        assertEquals(_10.getId(), "switch_1.0");
        assertEquals(_10.children.size(), 1);

        DirectedPort _100 = _10.children.get(0);
        assertEquals(_100.getId(), "switch_1.1");
        assertEquals(_100.children.size(), 1);

        DirectedPort _1000 = _100.children.get(0);
        assertEquals(_1000.getId(), "bus_1.0");
        assertEquals(_1000.children.size(), 2);

        DirectedPort _10000 = _1000.children.get(0);
        assertEquals(_10000.getId(), "switch_2.1");
        assertTrue(_10000.children.isEmpty());

        DirectedPort _10001 = _1000.children.get(1);
        assertEquals(_10001.getId(), "load_1.0");
        assertTrue(_10001.children.isEmpty());
    }

    @Test
    public void genForest8() {
        List<PowerSupplySystemTree> forest = graph.genForest(new HashMap<>() {
            {
                put("switch_0", false);
                put("switch_1", true);
                put("switch_2", true);
            }
        });

        assertEquals(forest.size(), 2);

        PowerSupplySystemTree tree0 = forest.get(0);
        assertEquals(tree0.getNodes().size(), 2);

        assertEquals(tree0.getNodes().keySet(), new HashSet<String>() {
            {
                add("power_supply_0.0");
                add("switch_0.0");
            }
        });

        DirectedPort _0 = tree0.getRoot();
        assertEquals(_0.getId(), "power_supply_0.0");
        assertEquals(_0.children.size(), 1);

        DirectedPort _00 = _0.children.get(0);
        assertEquals(_00.getId(), "switch_0.0");
        assertTrue(_00.children.isEmpty());

        PowerSupplySystemTree tree1 = forest.get(1);

        assertEquals(tree1.getNodes().size(), 10);

        assertEquals(tree1.getNodes().keySet(), new HashSet<String>() {
            {
                add("power_supply_1.0");
                add("switch_1.0");
                add("switch_1.1");
                add("bus_1.0");
                add("switch_2.1");
                add("switch_2.0");
                add("bus_0.0");
                add("switch_0.1");
                add("load_0.0");
                add("load_1.0");
            }
        });

        DirectedPort _1 = tree1.getRoot();
        assertEquals(_1.getId(), "power_supply_1.0");
        assertEquals(_1.children.size(), 1);

        DirectedPort _10 = _1.children.get(0);
        assertEquals(_10.getId(), "switch_1.0");
        assertEquals(_10.children.size(), 1);

        DirectedPort _100 = _10.children.get(0);
        assertEquals(_100.getId(), "switch_1.1");
        assertEquals(_100.children.size(), 1);

        DirectedPort _1000 = _100.children.get(0);
        assertEquals(_1000.getId(), "bus_1.0");
        assertEquals(_1000.children.size(), 2);

        DirectedPort _10000 = _1000.children.get(0);
        assertEquals(_10000.getId(), "switch_2.1");
        assertEquals(_10000.children.size(), 1);

        DirectedPort _100000 = _10000.children.get(0);
        assertEquals(_100000.getId(), "switch_2.0");
        assertEquals(_100000.children.size(), 1);

        DirectedPort _1000000 = _100000.children.get(0);
        assertEquals(_1000000.getId(), "bus_0.0");
        assertEquals(_1000000.children.size(), 2);

        DirectedPort _10000000 = _1000000.children.get(0);
        assertEquals(_10000000.getId(), "switch_0.1");
        assertTrue(_10000000.children.isEmpty());

        DirectedPort _10000001 = _1000000.children.get(1);
        assertEquals(_10000001.getId(), "load_0.0");
        assertTrue(_10000001.children.isEmpty());

        DirectedPort _10001 = _1000.children.get(1);
        assertEquals(_10001.getId(), "load_1.0");
        assertTrue(_10001.children.isEmpty());
    }

    @Test
    public void genForest9() {
        List<PowerSupplySystemTree> forest = graph.genForest(new HashMap<>() {
            {
                put("switch_0", true);
                put("switch_1", false);
                put("switch_2", true);
            }
        });

        assertEquals(forest.size(), 2);

        PowerSupplySystemTree tree0 = forest.get(0);

        assertEquals(tree0.getNodes().size(), 10);
        assertEquals(tree0.getNodes().keySet(), new HashSet<String>() {
            {
                add("power_supply_0.0");
                add("switch_0.0");
                add("switch_0.1");
                add("bus_0.0");
                add("load_0.0");
                add("switch_2.0");
                add("switch_2.1");
                add("bus_1.0");
                add("switch_1.1");
                add("load_1.0");
            }
        });

        DirectedPort _0 = tree0.getRoot();
        assertEquals(_0.getId(), "power_supply_0.0");
        assertEquals(_0.children.size(), 1);

        DirectedPort _00 = _0.children.get(0);
        assertEquals(_00.getId(), "switch_0.0");
        assertEquals(_00.children.size(), 1);

        DirectedPort _000 = _00.children.get(0);
        assertEquals(_000.getId(), "switch_0.1");
        assertEquals(_000.children.size(), 1);

        DirectedPort _0000 = _000.children.get(0);
        assertEquals(_0000.getId(), "bus_0.0");
        assertEquals(_0000.children.size(), 2);

        DirectedPort _00000 = _0000.children.get(0);
        assertEquals(_00000.getId(), "switch_2.0");
        assertEquals(_00000.children.size(), 1);

        DirectedPort _00001 = _0000.children.get(1);
        assertEquals(_00001.getId(), "load_0.0");
        assertTrue(_00001.children.isEmpty());

        DirectedPort _000000 = _00000.children.get(0);
        assertEquals(_000000.getId(), "switch_2.1");
        assertEquals(_000000.children.size(), 1);

        DirectedPort _0000000 = _000000.children.get(0);
        assertEquals(_0000000.getId(), "bus_1.0");
        assertEquals(_0000000.children.size(), 2);

        DirectedPort _00000000 = _0000000.children.get(0);
        assertEquals(_00000000.getId(), "switch_1.1");
        assertTrue(_00000000.children.isEmpty());

        DirectedPort _00000001 = _0000000.children.get(1);
        assertEquals(_00000001.getId(), "load_1.0");
        assertTrue(_00000001.children.isEmpty());
    }

    @Test
    public void genForestWithDcDc() {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        graph.addDevice(new PowerSupply("power_supply"));
        graph.addDevice(new DcDc("dc_dc"));
        graph.addEdge("power_supply.0", "dc_dc.0");
        graph.addDevice(new Switch("switch"));
        graph.addEdge("dc_dc.1", "switch.0");
        graph.addDevice(new Load("load"));
        graph.addEdge("switch.1", "load.0");

        List<PowerSupplySystemTree> forest = graph.genForest(new HashMap<>() {
            {
                put("switch", true);
            }
        });

        assertEquals(forest.size(), 1);
        PowerSupplySystemTree tree = forest.get(0);
        assertEquals(tree.getNodes().size(), 6);

        assertEquals(tree.getNodes().keySet(), new HashSet<String>() {
            {
                add("power_supply.0");
                add("dc_dc.0");
                add("dc_dc.1");
                add("switch.0");
                add("switch.1");
                add("load.0");
            }
        });

        DirectedPort _0 = tree.getRoot();
        assertEquals(_0.getId(), "power_supply.0");
        assertEquals(_0.children.size(), 1);

        DirectedPort _00 = _0.children.get(0);
        assertEquals(_00.getId(), "dc_dc.0");
        assertEquals(_00.children.size(), 1);

        DirectedPort _000 = _00.children.get(0);
        assertEquals(_000.getId(), "dc_dc.1");
        assertEquals(_000.children.size(), 1);

        DirectedPort _0000 = _000.children.get(0);
        assertEquals(_0000.getId(), "switch.0");
        assertEquals(_0000.children.size(), 1);

        DirectedPort _00000 = _0000.children.get(0);
        assertEquals(_00000.getId(), "switch.1");
        assertEquals(_00000.children.size(), 1);

        DirectedPort _000000 = _00000.children.get(0);
        assertEquals(_000000.getId(), "load.0");
        assertTrue(_000000.children.isEmpty());
    }

    @Test
    public void findPassages1() {
        Map<String, List<Passage>> passages = graph.findPassages(Arrays.asList(
                Pair.create("load_0", 0), Pair.create("load_1", 0)), new HashMap<>() {
                    {
                        put("switch_0", true);
                        put("switch_1", true);
                        put("switch_2", false);
                    }
                });

        assertEquals(passages.size(), 2);
        assertEquals(passages.get("load_0.0").size(), 1);
        assertEquals(passages.get("load_1.0").size(), 1);

        assertEquals(passages.get("load_0.0").get(0), (Arrays.asList(
                Pair.create("power_supply_0", 0), Pair.create("switch_0", 0),
                Pair.create("switch_0", 1), Pair.create("bus_0", 0),
                Pair.create("load_0", 0))));

        assertEquals(passages.get("load_1.0").get(0), (Arrays.asList(
                Pair.create("power_supply_1", 0), Pair.create("switch_1", 0),
                Pair.create("switch_1", 1), Pair.create("bus_1", 0),
                Pair.create("load_1", 0))));
    }

    @Test
    public void findPassages2() {
        Map<String, List<Passage>> passages = graph.findPassages(Arrays.asList(
                Pair.create("load_0", 0), Pair.create("load_1", 0)), new HashMap<>() {
                    {
                        put("switch_0", true);
                        put("switch_1", false);
                        put("switch_2", true);
                    }
                });

        assertEquals(passages.size(), 2);
        assertEquals(passages.get("load_0.0").size(), 1);
        assertEquals(passages.get("load_1.0").size(), 1);

        assertEquals(passages.get("load_0.0").get(0), (Arrays.asList(
                Pair.create("power_supply_0", 0), Pair.create("switch_0", 0),
                Pair.create("switch_0", 1), Pair.create("bus_0", 0),
                Pair.create("load_0", 0))));

        assertEquals(passages.get("load_1.0").get(0), (Arrays.asList(
                Pair.create("power_supply_0", 0), Pair.create("switch_0", 0),
                Pair.create("switch_0", 1), Pair.create("bus_0", 0),
                Pair.create("switch_2", 0), Pair.create("switch_2", 1),
                Pair.create("bus_1", 0),
                Pair.create("load_1", 0))));
    }

    @Test
    public void findPassages3() {
        Map<String, List<Passage>> passages = graph.findPassages(Arrays.asList(
                Pair.create("load_0", 0), Pair.create("load_1", 0)), new HashMap<>() {
                    {
                        put("switch_0", false);
                        put("switch_1", true);
                        put("switch_2", true);
                    }
                });

        assertEquals(passages.size(), 2);
        assertEquals(passages.get("load_0.0").size(), 1);
        assertEquals(passages.get("load_1.0").size(), 1);

        assertEquals(passages.get("load_0.0").get(0), (Arrays.asList(
                Pair.create("power_supply_1", 0), Pair.create("switch_1", 0),
                Pair.create("switch_1", 1), Pair.create("bus_1", 0),
                Pair.create("switch_2", 1), Pair.create("switch_2", 0),
                Pair.create("bus_0", 0),
                Pair.create("load_0", 0))));

        assertEquals(passages.get("load_1.0").get(0), (Arrays.asList(
                Pair.create("power_supply_1", 0), Pair.create("switch_1", 0),
                Pair.create("switch_1", 1), Pair.create("bus_1", 0),
                Pair.create("load_1", 0))));
    }

    @Test
    public void findPassages4() {

        Map<String, List<Passage>> passages = graph.findPassages(Arrays.asList(
                Pair.create("load_0", 0), Pair.create("load_1", 0)), new HashMap<>() {
                    {
                        put("switch_0", true);
                        put("switch_1", false);
                        put("switch_2", false);
                    }
                });
        assertEquals(passages.size(), 1);

        assertFalse(passages.containsKey("load_1.0"));
        assertEquals(passages.get("load_0.0").size(), 1);

        assertEquals(passages.get("load_0.0").get(0), (Arrays.asList(
                Pair.create("power_supply_0", 0), Pair.create("switch_0", 0),
                Pair.create("switch_0", 1), Pair.create("bus_0", 0),
                Pair.create("load_0", 0))));

    }

    @Test
    public void findPassges5() {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();
        graph.addDevice(new PowerSupply("power_supply_0"));
        graph.addDevice(new PowerSupply("power_supply_1"));

        graph.addDevice(new Switch("switch_0"));
        graph.addEdge("power_supply_0.0", "switch_0.0");
        graph.addDevice(new Switch("switch_1"));
        graph.addEdge("power_supply_1.0", "switch_1.0");

        graph.addDevice(new Bus("bus_0"));
        graph.addEdge("switch_0.1", "bus_0.0");
        graph.addDevice(new Bus("bus_1"));
        graph.addEdge("switch_1.1", "bus_1.0");

        graph.addDevice(new Diode("diode_0"));
        graph.addEdge("bus_0.0", "diode_0.0");
        graph.addDevice(new Diode("diode_1"));
        graph.addEdge("bus_1.0", "diode_1.0");

        graph.addDevice(new Load("load"));
        graph.addEdge("diode_0.1", "load.0");
        graph.addEdge("diode_1.1", "load.0");

        Map<String, List<Passage>> passages = graph.findPassages(Arrays.asList(
                Pair.create("load", 0)), new HashMap<>() {
                    {
                        put("switch_0", true);
                        put("switch_1", true);
                    }
                });

        assertEquals(passages.size(), 1);
        assertEquals(passages.get("load.0").size(), 2);

        assertEquals(passages.get("load.0").get(0), (Arrays.asList(
                Pair.create("power_supply_0", 0), Pair.create("switch_0", 0),
                Pair.create("switch_0", 1), Pair.create("bus_0", 0),
                Pair.create("diode_0", 0), Pair.create("diode_0", 1),
                Pair.create("load", 0))));

        assertEquals(passages.get("load.0").get(1), (Arrays.asList(
                Pair.create("power_supply_1", 0), Pair.create("switch_1", 0),
                Pair.create("switch_1", 1), Pair.create("bus_1", 0),
                Pair.create("diode_1", 0), Pair.create("diode_1", 1),
                Pair.create("load", 0))));
    }

}

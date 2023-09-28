package pssm.benchmarks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import pssm.PowerSupplySystemGraph;
import pssm.devices.BaseDevice;
import pssm.devices.Bus;
import pssm.devices.DcDc;
import pssm.devices.Load;
import pssm.devices.PowerSupply;
import pssm.devices.Switch;

class FindPassagesBenchmark {
    private static final int RUNS = 100;

    private static PowerSupplySystemGraph buildGraph(int level1, int level2) {
        PowerSupplySystemGraph graph = new PowerSupplySystemGraph();

        graph.addDevice(new PowerSupply("power_supply_0"));
        graph.addDevice(new PowerSupply("power_supply_1"));

        graph.addDevice(new DcDc("dc_dc_0"));
        graph.addEdge("power_supply_0.0", "dc_dc_0.0");
        graph.addDevice(new DcDc("dc_dc_1"));
        graph.addEdge("power_supply_1.0", "dc_dc_1.0");

        graph.addDevice(new Switch("switch_0", true));
        graph.addEdge("dc_dc_0.0", "switch_0.0");
        graph.addDevice(new Switch("switch_1", true));
        graph.addEdge("dc_dc_1.0", "switch_1.0");

        for (int i = 0; i < level1; ++i) {
            graph.addDevice(new Bus("bus_0_" + i));
            graph.addEdge("switch_0.1", "bus_0_" + i + ".0");

            graph.addDevice(new Bus("bus_1_" + i));
            graph.addEdge("switch_1.1", "bus_1_" + i + ".0");

            for (int j = 0; j < level2; j++) {
                graph.addDevice(new Switch("switch_0_" + i + "_" + j, true));
                graph.addEdge("bus_0_" + i + ".0", "switch_0_" + i + "_" + j + ".0");
                graph.addDevice(new Load("load_0_" + i + "_" + j));
                graph.addEdge("switch_0_" + i + "_" + j + ".1", "load_0_" + i + "_" + j + ".0");

                graph.addDevice(new Switch("switch_1_" + i + "_" + j, true));
                graph.addEdge("bus_1_" + i + ".0", "switch_1_" + i + "_" + j + ".0");
                graph.addDevice(new Load("load_1_" + i + "_" + j));
                graph.addEdge("switch_1_" + i + "_" + j + ".1", "load_1_" + i + "_" + j + ".0");
            }
        }

        return graph;
    }

    public static void main(String[] args) {
        List<Result> results = new ArrayList<>();
        for (Pair<Integer, Integer> pair : Arrays.asList(Pair.create(50, 50), Pair.create(100, 100))) {
            PowerSupplySystemGraph graph = buildGraph(pair.getFirst(), pair.getSecond());
            List<Pair<String, Integer>> destinations = new ArrayList<>();
            // collection 100 loads
            for (BaseDevice d : graph.getDevices().values()) {
                if (d instanceof Load) {
                    destinations.add(Pair.create(d.name, 0));
                }
                if (destinations.size() == 100) {
                    break;
                }
            }

            long start = System.currentTimeMillis();
            for (int i = 0; i < RUNS; ++i) {
                graph.findPassages(destinations);
            }
            long duration = Math.round(System.currentTimeMillis() - start);
            Result result = new Result(
                    RUNS,
                    graph.getPorts().size(),
                    graph.getEdges().size(),
                    duration,
                    Math.round(duration / RUNS));

            results.add(result);
        }

        final Object[][] table = new String[results.size() + 1][];
        table[0] = new String[] { "RUNS", "NODES", "EDGES", "TOTAL(ms)", "AVG(ms)" };
        for (int i = 0; i < results.size(); ++i) {
            Result result = results.get(i);
            table[i + 1] = new String[] {
                    String.valueOf(result.runs),
                    String.valueOf(result.nodes),
                    String.valueOf(result.edges),
                    String.valueOf(result.total),
                    String.valueOf(result.avg)
            };
        }
        for (final Object[] row : table) {
            System.out.format("%15s%15s%15s%15s%15s%n", row);
        }
    }
}

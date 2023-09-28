package pssm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import pssm.devices.BaseDevice;

final public class Port {
    public BaseDevice device;
    public int index;
    public List<Pair<Port, Object>> adjList;

    public String getId() {
        return this.device.name + "." + this.index;
    }

    public Port(BaseDevice device, int index) {
        this.device = device;
        this.index = index;
        this.adjList = new ArrayList<>();
    }
}

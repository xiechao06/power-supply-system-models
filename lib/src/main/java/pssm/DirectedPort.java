package pssm;

import java.util.ArrayList;
import java.util.List;

import pssm.devices.BaseDevice;

public final class DirectedPort {
    public final BaseDevice device;
    public final int portIndex;
    public final List<DirectedPort> children;
    public final List<DirectedEdge> edges;

    private DirectedPort parent;

    public DirectedPort getParent() {
        return parent;
    }

    public void setParent(DirectedPort parent) {
        this.parent = parent;
    }

    public DirectedPort(BaseDevice device, int portIndex) {
        this.device = device;
        this.portIndex = portIndex;
        this.children = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public String getId() {
        return Utils.genPortId(device.name, portIndex);
    }
}
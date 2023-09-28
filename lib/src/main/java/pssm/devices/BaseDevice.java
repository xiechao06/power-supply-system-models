package pssm.devices;

public abstract class BaseDevice {
    public String name;

    public BaseDevice(String name) {
        this.name = name;
    }

    public abstract int getNumPorts();
}

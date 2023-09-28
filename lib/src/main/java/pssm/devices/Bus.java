package pssm.devices;

public final class Bus extends BaseDevice {

    public Bus(String name) {
        super(name);
    }

    @Override
    public int getNumPorts() {
        return 1;
    }
}
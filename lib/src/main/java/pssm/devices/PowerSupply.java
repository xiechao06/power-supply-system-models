package pssm.devices;

public final class PowerSupply extends BaseDevice {

    public PowerSupply(String name) {
        super(name);
    }

    @Override
    public int getNumPorts() {
        return 1;
    }
}
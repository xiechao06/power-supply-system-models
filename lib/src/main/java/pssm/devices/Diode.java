package pssm.devices;

public final class Diode extends BaseDevice {

    public Diode(String name) {
        super(name);
    }

    @Override
    public int getNumPorts() {
        return 2;
    }
}
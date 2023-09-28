package pssm.devices;

public final class DcDc extends BaseDevice {
    public DcDc(String name) {
        super(name);
    }

    @Override
    public int getNumPorts() {
        return 2;
    }
}
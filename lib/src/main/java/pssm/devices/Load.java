package pssm.devices;

public final class Load extends BaseDevice {

    public Load(String name) {
        super(name);
    }

    @Override
    public int getNumPorts() {
        return 1;
    }
}
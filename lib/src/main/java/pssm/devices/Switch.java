package pssm.devices;

public final class Switch extends BaseDevice {

    private boolean isClosed = false;

    public boolean isClosed() {
        return isClosed;
    }

    public Switch(String name) {
        this(name, false);
    }

    public Switch(String name, boolean isClosed) {
        super(name);
        this.isClosed = isClosed;
    }

    @Override
    public int getNumPorts() {
        return 2;
    }

    public void close() {
        this.isClosed = true;
    }

    public void open() {
        this.isClosed = false;
    }
}
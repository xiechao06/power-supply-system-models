package pssm.exceptions;

public class NoSuchDevice extends IllegalArgumentException {
    public String deviceName;

    public NoSuchDevice(String name) {
        super("No such device: " + name);
        this.deviceName = name;
    }
}

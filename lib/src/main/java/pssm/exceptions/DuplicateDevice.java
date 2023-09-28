package pssm.exceptions;

public class DuplicateDevice extends IllegalArgumentException {
    public String name;

    public DuplicateDevice(String name) {
        super("Duplicate device: " + name);
        this.name = name;
    }
}

package pssm.exceptions;

public final class LackPowerSupplies extends IllegalArgumentException {

    public LackPowerSupplies() {
        super("Lack power supplies");
    }
}
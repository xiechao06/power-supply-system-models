package pssm.exceptions;

import pssm.devices.BaseDevice;

public class ChargePowerSupply extends IllegalArgumentException {
    public final BaseDevice from;
    public final BaseDevice to;

    public ChargePowerSupply(BaseDevice from, BaseDevice to) {
        super("Charge power supply: from " + from.name + " to " + to.name);
        this.from = from;
        this.to = to;
    }
}

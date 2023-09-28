package pssm;

import org.apache.commons.math3.util.Pair;

public class Utils {
    public static String genPortId(String deviceName, int portIndex) {
        return deviceName + "." + portIndex;
    }

    public static String genPortId(Pair<String, Integer> port) {
        return genPortId(port.getFirst(), port.getSecond());
    }
}
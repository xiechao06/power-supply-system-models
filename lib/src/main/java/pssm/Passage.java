package pssm;

import java.util.ArrayList;

import org.apache.commons.math3.util.Pair;

/**
 * A Passage is a list of pairs, where each pair consists of a device name and
 * an
 * Port.
 * 
 * Don't panic! This is just a type alias for ArrayList<Pair<String, Integer>>.
 * see https://stackoverflow.com/a/1195242/2687929
 */
public final class Passage extends ArrayList<Pair<String, Integer>> {
}
package pssm;

public final class DirectedEdge {
    private final DirectedPort from;

    public DirectedPort getFrom() {
        return from;
    }

    private final DirectedPort to;

    public DirectedPort getTo() {
        return to;
    }

    private final Object extras;

    public Object getExtras() {
        return extras;
    }

    public DirectedEdge(DirectedPort from, DirectedPort to, Object extras) {
        this.from = from;
        this.to = to;
        this.extras = extras;
    }
}
package pssm;

final public class Edge {
    public Port first;
    public Port second;
    public Object extras;

    public Edge(Port first, Port second) {
        this(first, second, null);
    }

    public Edge(Port first, Port second, Object extras) {
        this.first = first;
        this.second = second;
        this.extras = extras;
    }
}

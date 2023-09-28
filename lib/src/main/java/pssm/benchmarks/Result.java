package pssm.benchmarks;

class Result {
    public int runs;
    public int nodes;
    public int edges;
    public long total;
    public long avg;

    public Result(int runs, int nodes, int edges, long total, long avg) {
        this.runs = runs;
        this.nodes = nodes;
        this.edges = edges;
        this.total = total;
        this.avg = avg;
    }
}

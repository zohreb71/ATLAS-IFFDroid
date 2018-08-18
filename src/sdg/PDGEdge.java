package sdg;

public class PDGEdge {
    public enum EdgeType {CTR_DEP, DATA_DEP, CALL};

    protected EdgeType type ;
    protected PDGNodeForIFC src ;
    protected PDGNodeForIFC dest;


    public PDGEdge( PDGNodeForIFC src, PDGNodeForIFC dest, EdgeType type) {
        this.type = type;
        this.src = src;
        this.dest = dest;
    }

    public EdgeType getType() {
        return type;
    }

    public PDGNodeForIFC getSrc() {
        return src;
    }

    public PDGNodeForIFC getDest() {
        return dest;
    }
}

package sdg;

import soot.Unit;

//Auxiliary edges to construct SDG from PDGs
public class SDGEdge {

    public enum Type {CALL, //Call_site ---> EntryPoint of target method
                      PARAM_IN, //Actual_in ---> Formal_in
                      PARAM_OUT }; // Formal_out ---> Actual_out

    SDGNode source = null ;
    SDGNode target = null ;
    Type type = null ;
    int srcPdgId ;
    int trgPdgId ;
    PDGNodeForIFC src ;
    PDGNodeForIFC trg ;

    public void addSDGEdge (SDGNode src,SDGNode trg){

    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public SDGNode getSource() {

        return source;
    }

    public SDGNode getTarget() {
        return target;
    }

    public void setSource(SDGNode source) {
        this.source = source;
    }

    public void setTarget(SDGNode target) {
        this.target = target;
    }

    public void addCallEdge (){
        type = Type.CALL ;

    }
}

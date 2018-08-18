package sdg;

import soot.Unit;
import soot.toolkits.graph.pdg.PDGNode;

public class SDGNode {

    public enum Type{REGION, CFGNODE,//Node types in a PDG
        ACT_OUT, ACT_IN, FOR_OUT, FOR_IN, CALL_SITE };//added node types to construct SDG from PDGs

    Type type ;
    Unit CorrespondingUnit ;

    public Type getType() {
        return type;
    }

    public Unit getCorrespondingUnit() {
        return CorrespondingUnit;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setCorrespondingUnit(Unit correspondingUnit) {
        CorrespondingUnit = correspondingUnit;
    }
}


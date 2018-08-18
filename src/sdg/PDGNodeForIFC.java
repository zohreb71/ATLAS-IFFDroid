package sdg;

import soot.JastAddJ.Binary;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.lattice.BinaryLattice;
import soot.jimple.toolkits.lattice.LatticeElement;
import soot.toolkits.graph.pdg.PDGNode;
import sdg.PDGforIFC.*;
import java.util.LinkedList;

public class PDGNodeForIFC {

    PDGNode node ;
    LinkedList<PDGNodeForIFC> children = new LinkedList<PDGNodeForIFC>() ;
    PDGNodeForIFC parent = null ;
    LinkedList<PDGEdge> incomingEdges;
    LinkedList<PDGEdge> outgoingEdges ;
    LinkedList<PDGEdge> callEdges;
    LinkedList<PDGEdge> calledByEdges;
    int pdgID ;
    LatticeElement provided = BinaryLattice.lowElement ;
    LatticeElement required ;
    boolean visited = false;
    boolean isSource = false ;
    boolean isSink = false ;
    boolean visitedInBS = false ;
    boolean visitedInFS = false ;

    public boolean isVisitedInFS() {
        return visitedInFS;
    }

    public void setVisitedInFS(boolean visitedInFS) {
        this.visitedInFS = visitedInFS;
    }

    public boolean isVisitedInBS() {
        return visitedInBS;
    }

    public void setVisitedInBS(boolean visitedInBS) {
        this.visitedInBS = visitedInBS;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public LinkedList<PDGEdge> getIncomingEdges() {
        return incomingEdges;
    }

    public LinkedList<PDGEdge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public void addToIncomingEdges(PDGEdge e){
        incomingEdges.add(e);
    }

    public void addToOutgoingEdges(PDGEdge e){
        outgoingEdges.add(e);
    }

    public LinkedList<PDGEdge> getCallEdges() {
        return callEdges;
    }

    public LinkedList<PDGEdge> getCalledByEdges() {
        return calledByEdges;
    }

    public void addToCallEdges(PDGEdge e) { callEdges.add(e); }

    public void addToCalledByEdges (PDGEdge e) { calledByEdges.add(e); }

    public PDGNodeForIFC(Unit u, PDGNode.Type t, int option, int PdgId) {

        incomingEdges = new LinkedList<PDGEdge>();
        outgoingEdges = new LinkedList<PDGEdge>();
        callEdges = new LinkedList<PDGEdge>();
        calledByEdges = new LinkedList<PDGEdge>();
        node = new PDGNode(u,t);
        pdgID = PdgId ;
        switch (option){
            case 0: {
                provided = BinaryLattice.lowElement;
                break;
            }
            case 1: {
                provided = BinaryLattice.highElement;
                isSource = true;
                break;
            }
            case -1:{
                required = BinaryLattice.lowElement;
                isSink = true ;
                break;
            }
        }
    }


    public PDGNodeForIFC(PDGNode n, int Pdgid){
        node=n;
        provided= BinaryLattice.lowElement;
        incomingEdges = new LinkedList<PDGEdge>();
        outgoingEdges = new LinkedList<PDGEdge>();
        callEdges = new LinkedList<PDGEdge>();
        calledByEdges = new LinkedList<PDGEdge>();
        pdgID = Pdgid ;
    }

    public void addchild (PDGNodeForIFC node){
        this.children.add(node);
    }

    public PDGNodeForIFC getParent() {
        return parent;
    }

    public void setParent(PDGNodeForIFC parent) {
        this.parent = parent;
    }
}

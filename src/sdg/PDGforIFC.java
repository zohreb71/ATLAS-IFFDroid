package sdg;

import soot.*;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.ide.exampleproblems.IFDSReachingDefinitions;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;
import soot.toolkits.scalar.Pair;
import soot.util.SingletonList;

import javax.print.attribute.HashPrintJobAttributeSet;
import java.util.*;
//import java.util.Collections.SingletonSet;

public class PDGforIFC {

    class shouldBeAddedEdge {
        Unit u ;
        PDGNode node ;

        public shouldBeAddedEdge(Unit u, PDGNode node) {
            this.u = u;
            this.node = node;
        }
    }
    class shoudBeAddedEdgeforRegions{
        PDGNode src ;
        PDGNode dst ;

        public shoudBeAddedEdgeforRegions(PDGNode src, PDGNode dst) {
            this.src = src;
            this.dst = dst;
        }
    }

    PDGNodeForIFC entryNode ;
    String methodName ;
    int pdgID ;
    int parentID ;
    LinkedList<PDGNodeForIFC> infoSinks = new LinkedList<PDGNodeForIFC>();
    //private LinkedList<Object> sinktmp = new LinkedList<Object>();
    LinkedList<PDGNodeForIFC> nodes = new LinkedList<PDGNodeForIFC>();
    public HashMap<Object,PDGNodeForIFC> unitToPDGNodeForIFC = new HashMap<Object,PDGNodeForIFC>();//it holds CFGNodes
    public HashMap<PDGNode,PDGNodeForIFC> pdgNodeToPdgNodeForIFC = new HashMap<PDGNode,PDGNodeForIFC>();//holds RegionNodes and Entry
    //LinkedHashMap<Unit,PDGNode> shouldbeAddedEdges = new LinkedHashMap<Unit,PDGNode>();
    LinkedList<shouldBeAddedEdge> shouldbeAddedEdges = new LinkedList<shouldBeAddedEdge>();
    LinkedList<shoudBeAddedEdgeforRegions> shoudBeAddedEdgesforRegions = new LinkedList<shoudBeAddedEdgeforRegions>();
    public HashMap<Object,PDGNodeForIFC> finalUnitToPDGNodeForIFC = new HashMap<Object,PDGNodeForIFC>();
    //HashMap<ValueBox,Unit> useBoxes = new HashMap<ValueBox,Unit>();

    public HashMap<Object, PDGNodeForIFC> getFinalUnitToPDGNodeForIFC() {
        return finalUnitToPDGNodeForIFC;
    }

    public PDGNodeForIFC searchForCorrespondingPDGNode(Object u){
        return finalUnitToPDGNodeForIFC.get(u);
    }

    public PDGforIFC(HashMutablePDG pdg, UnitGraph ug, Set<Stmt> sources, Set<Stmt> sinks, int id){
        parentID = pdg.getParentId();
        addControlDependence(pdg, sources, sinks);
        addDataDependence(ug);
        setEntry();
        setFinalUnitToPDGNodeForIFC();
        //setInformationSinks();
        methodName = nameBuilder(pdg);
        pdgID = id ;

    }

    public void addControlDependence(HashMutablePDG pdg, Set<Stmt> sources, Set<Stmt> sinks ) {
        LinkedList<PDGNode> toProcess = new LinkedList<PDGNode>();
        toProcess.add(pdg.GetStartNode());

        //iterate over previous PDG
        while (!toProcess.isEmpty()){
            PDGNode old = toProcess.remove(0);

             if (old.getType() == PDGNode.Type.REGION){
                PDGNodeForIFC newParent = new PDGNodeForIFC(old,pdgID);
                pdgNodeToPdgNodeForIFC.put(old,newParent);
                old.setSdg_visited(true);
                nodes.add(newParent);
                List<PDGNode> dep = old.getDependents();

                while (!dep.isEmpty()){ //add edges to pdg
                    PDGNode node = dep.remove(0);
                    if (node.getType()== PDGNode.Type.CFGNODE && node.getNode() instanceof Block){
                        if (!node.getDependents().isEmpty()){
                            for (PDGNode n:node.getDependents()) { //a similar for loop should be add for backdependents
                                if (!n.isSdg_visited()) {
                                    toProcess.add(n);
                                    shouldBeAddedEdge edge = new shouldBeAddedEdge(((Block) node.getNode()).getTail(),n);
                                    shouldbeAddedEdges.add(edge);
                                }
                                else {
                                    shouldBeAddedEdge edge = new shouldBeAddedEdge(((Block) node.getNode()).getTail(),n);
                                    shouldbeAddedEdges.add(edge);
                                }
                            }
                            for (PDGNode n:node.getBackDependets()) { //a similar for loop should be add for backdependents
                                if (!n.isSdg_visited()) {
                                    toProcess.add(n);
                                    shouldBeAddedEdge edge = new shouldBeAddedEdge(((Block) node.getNode()).getTail(),n);
                                    shouldbeAddedEdges.add(edge);
                                }
                                else {
                                    shouldBeAddedEdge edge = new shouldBeAddedEdge(((Block) node.getNode()).getTail(),n);
                                    shouldbeAddedEdges.add(edge);
                                }
                            }
                        }
                        Iterator<Unit> newChildren = ((Block) node.getNode()).iterator();
                        while (newChildren.hasNext()) {
                            Unit un = newChildren.next();
                            PDGNodeForIFC newchild;
                            if (sources.contains(un)) {
                                newchild = new PDGNodeForIFC(un, PDGNode.Type.CFGNODE, 1, pdgID);
                            }
                            else if (sinks.contains(un)) {
                                newchild = new PDGNodeForIFC(un, PDGNode.Type.CFGNODE, -1, pdgID);
                                infoSinks.add(newchild);
                            }
                            else {
                                newchild = new PDGNodeForIFC(un, PDGNode.Type.CFGNODE,0,pdgID);
                            }
                            unitToPDGNodeForIFC.put(un,newchild);
                            nodes.add(newchild);
                            addEdge(newParent,newchild, PDGEdge.EdgeType.CTR_DEP);

                        }
                    }
                    else if (node.getType()== PDGNode.Type.REGION && !node.isSdg_visited()){
                        shoudBeAddedEdgeforRegions edge = new shoudBeAddedEdgeforRegions(old,node);
                        shoudBeAddedEdgesforRegions.add(edge);
                        toProcess.addLast(node);

                    }
                }

            }

        }
        if (!shouldbeAddedEdges.isEmpty()){
            Iterator<shouldBeAddedEdge> itr = shouldbeAddedEdges.iterator();
            while (itr.hasNext()){
                shouldBeAddedEdge e = itr.next();
                Unit u = e.u ;
                PDGNode p = e.node ;
                addEdge(unitToPDGNodeForIFC.get(u),pdgNodeToPdgNodeForIFC.get(p), PDGEdge.EdgeType.CTR_DEP);
            }


        }
        if (!shoudBeAddedEdgesforRegions.isEmpty()){
            Iterator<shoudBeAddedEdgeforRegions> itr = shoudBeAddedEdgesforRegions.iterator();
            while (itr.hasNext()){
                shoudBeAddedEdgeforRegions e = itr.next();
                PDGNode src = e.src;
                PDGNode dst = e.dst;
                addEdge(pdgNodeToPdgNodeForIFC.get(src),pdgNodeToPdgNodeForIFC.get(dst), PDGEdge.EdgeType.CTR_DEP);
            }
        }
    }

;
    public void addDataDependence(UnitGraph ug){

        /*JimpleBasedInterproceduralCFG reachDefsICFG = new JimpleBasedInterproceduralCFG();
        IFDSReachingDefinitions problem = new IFDSReachingDefinitions(reachDefsICFG);
        heros.solver.IFDSSolver solver = new heros.solver.IFDSSolver(problem);
        solver.solve();*/

        Iterator<PDGNodeForIFC> itr = nodes.iterator();

        //*******************Find Data Dependencies**************/
        while (itr.hasNext()){

            PDGNodeForIFC next = itr.next();

            if (next.node.getType()== PDGNode.Type.REGION)
                continue;

            else {

                Unit u = (Unit) next.node.getNode();

                if (u instanceof JAssignStmt && ((JAssignStmt) u).rightBox.getValue() instanceof JInstanceFieldRef) {
                    List<JInstanceFieldRef> used = new LinkedList<>();
                    Iterator<ValueBox> tr = u.getUseBoxes().iterator();
                    while (tr.hasNext()) {
                        ValueBox v = tr.next();
                        if (v.getValue() instanceof JInstanceFieldRef)
                            used.add((JInstanceFieldRef) v.getValue());
                    }
                    Map<JInstanceFieldRef,Unit> def = findDefsReachSpecialStmt(ug,u,used);
                    for (Unit d:def.values())
                        addEdge(unitToPDGNodeForIFC.get(d),next, PDGEdge.EdgeType.DATA_DEP);
                }

                else {
                    List<ValueBox> usedVariables = u.getReleventUseBoxes();

                if (!usedVariables.isEmpty()){
                    Map<ValueBox,Unit> def = findDefsReachesEachStmt(ug, u,usedVariables);
                    for (Unit d : def.values())
                        addEdge(unitToPDGNodeForIFC.get(d),next, PDGEdge.EdgeType.DATA_DEP);

                    }
                }

                }
            System.out.println(next.node.toString());
            }

        //addAdditionalEdges();
        }

   /* public void addAdditionalEdges (){
        String s = "$r8 = new de.ecspride.ImplicitFlow2$Poly_0";
        String d = "$r3[0] = $r8";
        PDGNodeForIFC sNode = unitToPDGNodeForIFC.get(s);
        PDGNodeForIFC dNode = unitToPDGNodeForIFC.get(d);
        addEdge(sNode,dNode, PDGEdge.EdgeType.DATA_DEP);
    }*/

    public HashMap<ValueBox,Unit> findDefsReachesEachStmt(UnitGraph ug, Unit u, List<ValueBox> usedvars) {

        HashMap<ValueBox, Unit> result = new HashMap<>();
        LinkedList<Unit> wl = new LinkedList<>();
        boolean foundedFlag ;

        for (ValueBox uv : usedvars) {
            if (uv.getValue() instanceof Constant)
                break;
            wl.clear();
            wl.addAll(ug.getPredsOf(u));
            foundedFlag = false ;

            while (!wl.isEmpty() && !foundedFlag){
                Unit cur = wl.removeFirst();
                if (!cur.getDefBoxes().isEmpty()) {
                    Iterator<ValueBox> defs = cur.getDefBoxes().iterator();
                    while (defs.hasNext()) {
                        ValueBox definedVar = defs.next();
                        if (definedVar.getValue() == uv.getValue()) {
                            result.put(uv, cur);
                            //i++;
                            foundedFlag=true ;
                            break;
                        }
                    }
                }
                if (!foundedFlag)
                    wl.addAll(ug.getPredsOf(cur));
            }
        }
        return result;
    }

    public HashMap<JInstanceFieldRef,Unit> findDefsReachSpecialStmt(UnitGraph ug, Unit u, List<JInstanceFieldRef> usedvars) {

        HashMap<JInstanceFieldRef, Unit> result = new HashMap<>();
        LinkedList<Unit> wl = new LinkedList<>();
        boolean foundedFlag ;

        for (JInstanceFieldRef uv : usedvars) {
            wl.clear();
            wl.addAll(ug.getPredsOf(u));
            foundedFlag = false ;

            while (!wl.isEmpty() && !foundedFlag){
                Unit cur = wl.removeFirst();
                if (!cur.getDefBoxes().isEmpty()) {
                    Iterator<ValueBox> defs = cur.getDefBoxes().iterator();
                    while (defs.hasNext()) {
                        ValueBox definedVar = defs.next();
                        if (definedVar.getValue() instanceof JInstanceFieldRef) {
                            result.put(uv, cur);
                            foundedFlag=true ;
                            break;
                        }
                    }
                }
                if (!foundedFlag)
                    wl.addAll(ug.getPredsOf(cur));
            }
        }
        return result;
    }
    public LinkedList<PDGNodeForIFC> getNodes() {
        return nodes;
    }

    public void setEntry() {
        entryNode = new PDGNodeForIFC(new JNopStmt(), PDGNode.Type.ENTRY, 0, pdgID);
        addEdge(entryNode,nodes.getFirst(), PDGEdge.EdgeType.CTR_DEP);
        nodes.addFirst(entryNode);
    }


    public LinkedList<PDGNodeForIFC> getInfoSinks() {
        return infoSinks;
    }

    void setFinalUnitToPDGNodeForIFC(){
        for (PDGNodeForIFC n:nodes ) {
            if (n.node.getType()!= PDGNode.Type.REGION)
                finalUnitToPDGNodeForIFC.put(n.node,n);
        }
    }

    public void addEdge(PDGNodeForIFC s, PDGNodeForIFC d, PDGEdge.EdgeType t){
        PDGEdge e = new PDGEdge(s,d,t);
        s.addToOutgoingEdges(e);
        d.addToIncomingEdges(e);
    }

    protected String nameBuilder(HashMutablePDG pdg){
        return pdg.getM_body().getMethod().getDeclaringClass().toString()+" : "+
                pdg.getM_body().getMethod().getName();
    }
}

package sdg;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;

import java.util.*;


public class SDG {

    PDGNodeForIFC root;
    HashMap<Integer, PDGforIFC> idToPdgIFC = new HashMap<Integer, PDGforIFC>();
    LinkedList<CallerToCallee> callEdges = new LinkedList<CallerToCallee>();
    LinkedList<PDGNodeForIFC> infoSnk = new LinkedList<PDGNodeForIFC>(); //information Sinks

    class CallerToCallee {
        Object InvokeStmt; //Unit of Invoke expression in caller method
        PDGforIFC srcPDG; //Id of caller method
        int trgPDGId; //Id of callee method

        public CallerToCallee(Unit invokeStmt, PDGforIFC srcPDG, int trgPDGId) {
            InvokeStmt = invokeStmt;
            this.srcPDG = srcPDG;
            this.trgPDGId = trgPDGId;
        }

        public Object getInvokeStmt() {
            return InvokeStmt;
        }

        public PDGforIFC getSrcPDG() {
            return srcPDG;
        }

        public int getTrgPDGId() {
            return trgPDGId;
        }
    }

    public SDG(InfoflowCFG apk_ICFG, Set<Stmt> sources, Set<Stmt> sinks) {
        constructSDG(apk_ICFG, sources, sinks);
        this.root = idToPdgIFC.get(0).entryNode;
        IFC IFCAnalysis = new IFC();
        IFCAnalysis.doIFC(this, infoSnk);
    }

    public PDGNodeForIFC getRoot() {
        return root;
    }

    public void constructSDG(InfoflowCFG apk_ICFG, Set<Stmt> sources, Set<Stmt> sinks) {
        //get PDG of main Method
        int id = 0;
        LinkedList<methodToParent> workList = new LinkedList<methodToParent>();
        workList.add(new methodToParent(Scene.v().getEntryPoints().get(0), id, -1));
        //id++;
        while (!workList.isEmpty()) {
            methodToParent w = workList.remove(0);
            SootMethod sm = w.getMethod();
            HashMutablePDG pdg;
            PDGforIFC newPDG;
            Iterator<Unit> itr;

            /*if (sm.getActiveBody().getTraps().isEmpty()) {
                DirectedGraph<Unit> dg = apk_ICFG.getOrCreateUnitGraph(sm);
                pdg = new HashMutablePDG((UnitGraph) dg, w.getMethodId(), w.getParentId()); //how to set parenthood relationships correctly
                newPDG = new PDGforIFC(pdg, (UnitGraph) dg, sources, sinks, id);
                itr = dg.iterator();
            } else {
                EnhancedUnitGraph enhancedUnitGraph = new EnhancedUnitGraph(sm.getActiveBody());
                pdg = new HashMutablePDG(enhancedUnitGraph, w.getMethodId(), w.getParentId());
                newPDG = new PDGforIFC(pdg, (EnhancedUnitGraph) enhancedUnitGraph, sources, sinks, id);
                itr = enhancedUnitGraph.iterator();

            }*/
            EnhancedUnitGraph enhancedUnitGraph = new EnhancedUnitGraph(sm.getActiveBody());
            pdg = new HashMutablePDG(enhancedUnitGraph, w.getMethodId(), w.getParentId());
            newPDG = new PDGforIFC(pdg, (EnhancedUnitGraph) enhancedUnitGraph, sources, sinks, id);
            itr = enhancedUnitGraph.iterator();


            infoSnk.addAll(newPDG.infoSinks);
            idToPdgIFC.put(id, newPDG);
            new PDGtoDot(newPDG, w.getMethodId());
            int newId = id;

            while (itr.hasNext()) {
                Unit u = itr.next();
                if (apk_ICFG.isCallStmt(u)) {

                    for (SootMethod target : apk_ICFG.getCalleesOfCallAt(u)) {//
                        if (target.hasActiveBody() && Scene.v().getApplicationClasses().contains(sm.getDeclaringClass())) {
                            newId++;
                            PDGNodeForIFC invExp = newPDG.unitToPDGNodeForIFC.get(u);
                            callEdges.add(new CallerToCallee(u, newPDG, newId));
                            workList.add(new methodToParent(target, newId, id));

                        }
                    }
                }
            }
            id++;
        }
        /*Iterator<CallerToCallee> cedge = callEdges.iterator();
        while (cedge.hasNext()){
            CallerToCallee cur = cedge.next();

            int trgID = cur.getTrgPDGId() ;
            PDGforIFC trgPdg = idToPdgIFC.get(trgID);
            PDGforIFC srcPdg = idToPdgIFC.get(trgPdg.parentID) ;
            PDGNodeForIFC caller = srcPdg.unitToPDGNodeForIFC.get(cur.getInvokeStmt());
            PDGNodeForIFC callee = trgPdg.entryNode ;
            addCallEdge(caller,callee);
            //PDGforIFC srcPdg = cur.getSrcPDG();
            //PDGforIFC srcPdg = idToPdgIFC.get(srcID);
        }*/

    }

    public void addCallEdge(PDGNodeForIFC src, PDGNodeForIFC trg) {
        PDGEdge edge = new PDGEdge(src, trg, PDGEdge.EdgeType.CALL);
        src.addToCallEdges(edge);
        trg.addToCalledByEdges(edge);

    }

    class methodToParent {
        SootMethod method;
        int methodId;
        int parentId;

        public methodToParent(SootMethod m, int id, int pId) {
            method = m;
            methodId = id;
            parentId = pId;
        }

        public SootMethod getMethod() {
            return method;
        }

        public int getMethodId() {
            return methodId;
        }

        public int getParentId() {
            return parentId;
        }
    }

    class PDGToParent {
        int PDGid;
        int parentID;
        PDGforIFC pdg;

        public PDGToParent(PDGforIFC pdg, int PDGid, int parentID) {
            this.PDGid = PDGid;
            this.parentID = parentID;
            this.pdg = pdg;
        }

        public int getPDGid() {
            return PDGid;
        }

        public int getParentID() {
            return parentID;
        }

        public PDGforIFC getPdg() {
            return pdg;
        }
    }


}

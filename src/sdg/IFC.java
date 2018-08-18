package sdg;

import soot.jimple.InvokeExpr;
import soot.toolkits.graph.pdg.PDGNode;
import java.util.LinkedList;

import static java.lang.System.exit;

public class IFC {
    LinkedList<InformationLeak> leaks = new LinkedList<InformationLeak>();
    LinkedList<ChopHelper> chops = new LinkedList<ChopHelper>();


    public void doIFC (SDG sdg, LinkedList<PDGNodeForIFC> sinks){
        ChopHelper chop ;
        for (PDGNodeForIFC snk : sinks) {
            chop=BackwardSlice(sdg, snk);
            ForwardSlice(chop);

        }
        chop();
        reportResults();
        exit(0);
    }


    public ChopHelper BackwardSlice (SDG sdg, PDGNodeForIFC criterion) {

        ChopHelper ch = new ChopHelper();
        LinkedList<PDGNodeForIFC> wl = new LinkedList<PDGNodeForIFC>();
        LinkedList<PDGNodeForIFC> visitingHelper = new LinkedList<>();
        wl.add(criterion);
        visitingHelper.add(criterion);
        LinkedList<PDGNodeForIFC> BSHelper = new LinkedList<PDGNodeForIFC>();
        //int srcCount = 0 ;
        BSHelper.add(criterion);
        while (!wl.isEmpty()){

            PDGNodeForIFC cur = wl.remove(0);
            cur.setVisitedInBS(true);

            if (cur.isSource){

                ch.setSnkCriterion(criterion);
                ch.setSrcCriterion(cur);
                ch.setBSofSnk(BSHelper);
                chops.add(ch);
            }
            //the else if has been added in order to handle call edges
            else if (wl.size()==0 && cur.node.getType()== PDGNode.Type.ENTRY){
                if (!cur.getCalledByEdges().isEmpty()){
                    for (PDGEdge e : cur.getCalledByEdges()){
                        PDGNodeForIFC sr = e.getSrc();
                        if (!sr.isVisitedInBS()){
                            if (sr.node.getType() != PDGNode.Type.REGION) {
                                BSHelper.addFirst(sr);
                            }
                            wl.add(sr);
                            visitingHelper.add(sr);
                            sr.setVisitedInBS(true);
                        }
                    }
                }
            }

            if (!cur.getIncomingEdges().isEmpty()) {
                for (PDGEdge e : cur.getIncomingEdges()) {
                    PDGNodeForIFC sr = e.getSrc();
                    if (!sr.isVisitedInBS()) {
                        if (sr.node.getType() != PDGNode.Type.REGION) {
                            BSHelper.addFirst(sr);
                        }
                        wl.add(sr);
                        visitingHelper.add(sr);
                        sr.setVisitedInBS(true);
                    }
                }
            }

        }
        for (PDGNodeForIFC n : visitingHelper)
            n.setVisitedInBS(false);
        return ch ;
    }

    void ForwardSlice(ChopHelper ch){
        if (ch == null){
            System.err.println("There is no chop Helper");
            return ;
        }
        else if (ch!= null && ch.getSnkCriterion() == null){
            System.err.println("no sinks found");
            return;
        }

        if (ch != null && ch.getSnkCriterion()!= null && ch.getSrcCriterion()!= null) {
            PDGNodeForIFC src = ch.srcCriterion;
            LinkedList<PDGNodeForIFC> wl = new LinkedList<PDGNodeForIFC>();
            LinkedList<PDGNodeForIFC> fSHelper = new LinkedList<PDGNodeForIFC>();
            LinkedList<PDGNodeForIFC> visitingHelper = new LinkedList<>();
            wl.add(src);
            visitingHelper.add(src);
            while (!wl.isEmpty()) {
                PDGNodeForIFC cur = wl.remove(0);
                if (!cur.isVisitedInFS()) {
                    cur.setVisitedInFS(true);
                    fSHelper.addLast(cur);
                }
                if (!cur.getOutgoingEdges().isEmpty()) {
                    for (PDGEdge e : cur.getOutgoingEdges()) {
                        PDGNodeForIFC dst = e.getDest();
                        if (!dst.isVisitedInFS()) {
                            if (dst.node.getType() != PDGNode.Type.REGION) {
                                fSHelper.addLast(dst);
                            }
                            wl.addLast(dst);
                            dst.setVisitedInFS(true);
                        }
                    }
                }

                //the else if has been added in order to handle call edges
                else if (wl.size()==0 && cur.node.getNode() instanceof InvokeExpr){
                    if (!cur.getCallEdges().isEmpty()){
                        for (PDGEdge e : cur.getCallEdges()){
                            PDGNodeForIFC dst = e.getDest();
                            if (!dst.isVisitedInBS()){
                                if (dst.node.getType() != PDGNode.Type.REGION) {
                                    fSHelper.addLast(dst);
                                }
                                wl.add(dst);
                                visitingHelper.add(dst);
                                dst.setVisitedInBS(true);
                            }
                        }
                    }
                }
            }

            ch.setFSofSrc(fSHelper);


            for(PDGNodeForIFC n:visitingHelper)
                n.setVisitedInFS(false);
        }
    }

    void chop (){
        LinkedList<PDGNodeForIFC> intersection ;
        for (ChopHelper ch: chops) {
            intersection = getIntersection(ch.getFSofSrc(),ch.getBSofSnk());
            ch.setChop(intersection);
        }
    }

    void reportResults (){

        if (chops.isEmpty()){
            System.out.println("We could not find any information leak in Android application");
            exit(0);
        }

        System.out.println("IFC Analysis Resutls: ");
        for (ChopHelper ch : chops ) {
            System.out.print("There is a flow from Source: " + ch.getSrcCriterion().node.getNode());
            System.out.println(" to Sink : "+ ch.getSnkCriterion().node.getNode());
            System.out.println("on Path");
            int i = 0 ;

            for (PDGNodeForIFC node: ch.getChop()) {
                System.out.print(node.node.getNode() + "-->");
            }
            System.out.println();
        }
    }

    static class InformationLeak {
        String srcMethodName;
        String sinkMethodName ;
        Object srcUnit ;
        Object snkUnit;

        public InformationLeak(String srcMethod, String sinkMethod, Object srcUnit, Object snkUnit) {
            this.srcMethodName = srcMethod;
            this.sinkMethodName = sinkMethod;
            this.srcUnit = srcUnit;
            this.snkUnit = snkUnit;
        }
    }

    //This class aids us to calculate the responsible nodes in information leaks
    class ChopHelper {
        PDGNodeForIFC srcCriterion ;
        LinkedList<PDGNodeForIFC> FSofSrc ;

        PDGNodeForIFC snkCriterion ;
        LinkedList<PDGNodeForIFC> BSofSnk ;
        LinkedList<PDGNodeForIFC> chop ;

        public LinkedList<PDGNodeForIFC> getChop() {
            return chop;
        }

        public void setChop(LinkedList<PDGNodeForIFC> chop) {
            this.chop = chop;
        }

        public ChopHelper() {
            srcCriterion = null ;
            snkCriterion = null ;
            FSofSrc = new LinkedList<PDGNodeForIFC>();
            BSofSnk = new LinkedList<PDGNodeForIFC>();
        }

        public void setSrcCriterion(PDGNodeForIFC srcCriterion) {
            this.srcCriterion = srcCriterion;
        }

        public void setFSofSrc(LinkedList<PDGNodeForIFC> FSofSrc) {
            this.FSofSrc = FSofSrc;
        }

        public void setSnkCriterion(PDGNodeForIFC snkCriterion) {
            this.snkCriterion = snkCriterion;
        }

        public void setBSofSnk(LinkedList<PDGNodeForIFC> BSofSnk) {
            this.BSofSnk = BSofSnk;
        }

        public PDGNodeForIFC getSrcCriterion() {
            return srcCriterion;
        }

        public LinkedList<PDGNodeForIFC> getFSofSrc() {
            return FSofSrc;
        }

        public PDGNodeForIFC getSnkCriterion() {
            return snkCriterion;
        }

        public LinkedList<PDGNodeForIFC> getBSofSnk() {
            return BSofSnk;
        }
    }

    LinkedList<PDGNodeForIFC> getIntersection (LinkedList<PDGNodeForIFC> list1, LinkedList<PDGNodeForIFC> list2){
        LinkedList<PDGNodeForIFC> result = new LinkedList<PDGNodeForIFC>();
        if (list1.size()<list2.size()){
            for (PDGNodeForIFC l1: list1) {
                if (list2.contains(l1))
                    result.add(l1);
            }
        }
        else {
            for (PDGNodeForIFC l2: list2) {
                if(list1.contains(l2))
                    result.add(l2);
            }
        }
        return result;
    }
}

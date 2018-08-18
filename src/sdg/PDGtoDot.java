package sdg;

import soot.toolkits.graph.pdg.PDGNode;
import soot.toolkits.graph.pdg.PDGRegion;
import soot.util.dot.DotGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class PDGtoDot {

    public PDGtoDot(PDGforIFC pdg, int id){
		DotGraph PDGinDot = new DotGraph("PDGinDot");

        LinkedList<PDGNodeForIFC> wl = new LinkedList<PDGNodeForIFC>();
        wl.add(pdg.nodes.get(1));

        while (!wl.isEmpty()) {
            PDGNodeForIFC cur = wl.remove(0);
            if (!cur.isVisited()) {
                if (cur.node.getType() == PDGNode.Type.REGION) {
                    PDGinDot.drawNode(cur.node.toString().substring(14, 26));
                    cur.setVisited(true);
                    for (PDGEdge e : cur.getOutgoingEdges()) {
                        if (e.type == PDGEdge.EdgeType.CTR_DEP) {
                            PDGNodeForIFC d = e.dest;
                            if (!d.isVisited()) {
                                PDGinDot.drawEdge(cur.node.toString().substring(14, 26), d.node.getNode().toString());
                                wl.add(d);
                                d.setVisited(true);
                            }
                        }
                    }
                }

                else if (cur.node.getType() == PDGNode.Type.CFGNODE) {
                    PDGinDot.drawNode(cur.node.getNode().toString());
                    cur.setVisited(true);
                    for (PDGEdge e : cur.getOutgoingEdges()) {
                        if (e.type == PDGEdge.EdgeType.CTR_DEP) {
                            PDGNodeForIFC d = e.dest;
                            if (!d.isVisited() && d.node.getType()== PDGNode.Type.CFGNODE) {
                                PDGinDot.drawEdge(cur.node.getNode().toString(), d.node.getNode().toString());
                                wl.add(d);
                                d.setVisited(true);
                            }

                            else if (!d.isVisited() && d.node.getType()== PDGNode.Type.REGION) {
                                PDGinDot.drawEdge(cur.node.getNode().toString(), d.node.toString().substring(14, 26));
                                wl.add(d);
                                d.setVisited(true);
                            }

                            else if (d.isVisited() && d.node.getType() == PDGNode.Type.CFGNODE){
                                PDGinDot.drawEdge(cur.node.getNode().toString(), d.node.getNode().toString());
                            }

                            else {
                                PDGinDot.drawEdge(cur.node.getNode().toString(), d.node.toString().substring(14, 26));
                            }
                        }
                        else if (e.type== PDGEdge.EdgeType.DATA_DEP){
                            PDGNodeForIFC d = e.dest;//DATA_DEP edges only exist between CFGNodes
                            if (!d.isVisited()){
                                PDGinDot.drawDataDepEdge(cur.node.getNode().toString(),d.node.getNode().toString());
                                d.setVisited(true);
                                wl.add(d);
                            }
                            else
                                PDGinDot.drawDataDepEdge(cur.node.getNode().toString(),d.node.getNode().toString());
                        }
                    }
                }
            }

            //******************************2****************************/
            else {
                if (cur.node.getType() == PDGNode.Type.REGION) {
                    PDGinDot.drawNode(cur.node.toString().substring(14, 26));
                    cur.setVisited(true);
                    for (PDGEdge e : cur.getOutgoingEdges()) {
                        if (e.type == PDGEdge.EdgeType.CTR_DEP) {
                            PDGNodeForIFC d = e.dest;
                            if (!d.isVisited()) {
                                PDGinDot.drawEdge(cur.node.toString().substring(14, 26), d.node.getNode().toString());
                                wl.add(d);
                                d.setVisited(true);
                            }
                        }
                    }
                }

                else if (cur.node.getType() == PDGNode.Type.CFGNODE) {
                    PDGinDot.drawNode(cur.node.getNode().toString());
                    cur.setVisited(true);
                    for (PDGEdge e : cur.getOutgoingEdges()) {
                        if (e.type == PDGEdge.EdgeType.CTR_DEP) {
                            PDGNodeForIFC d = e.dest;
                            if (!d.isVisited() && d.node.getType()== PDGNode.Type.CFGNODE) {
                                PDGinDot.drawEdge(cur.node.getNode().toString(), d.node.getNode().toString());
                                wl.add(d);
                                d.setVisited(true);
                            }

                            else if (!d.isVisited() && d.node.getType()== PDGNode.Type.REGION) {
                                PDGinDot.drawEdge(cur.node.getNode().toString(), d.node.toString().substring(14, 26));
                                wl.add(d);
                                d.setVisited(true);
                            }

                            else if (d.isVisited() && d.node.getType() == PDGNode.Type.CFGNODE){
                                PDGinDot.drawEdge(cur.node.getNode().toString(), d.node.getNode().toString());
                            }

                            else {
                                PDGinDot.drawEdge(cur.node.getNode().toString(), d.node.toString().substring(14, 26));
                            }
                        }
                        else if (e.type== PDGEdge.EdgeType.DATA_DEP){
                            PDGNodeForIFC d = e.dest;//DATA_DEP edges only exist between CFGNodes
                            if (!d.isVisited()){
                                PDGinDot.drawDataDepEdge(cur.node.getNode().toString(),d.node.getNode().toString());
                                d.setVisited(true);
                                wl.add(d);
                            }
                            else
                                PDGinDot.drawDataDepEdge(cur.node.getNode().toString(),d.node.getNode().toString());
                        }
                    }
                }
        }
        }
        PDGinDot.drawEntryNode("Entry node: " + id);
        PDGinDot.drawEdge("Entry node: "+id, pdg.nodes.get(1).node.toString().substring(14,26));

		FileOutputStream myfile = null;
		File dotFile = new File("/opt/pdgs/PDG"+"method"+id+"inDot.dot");
        try {
            myfile = new FileOutputStream(dotFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
			PDGinDot.render(myfile,32);
		} catch (IOException e) {
			e.printStackTrace();
		}



}

}

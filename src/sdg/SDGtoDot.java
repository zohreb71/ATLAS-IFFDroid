package sdg;

import java.io.File;
import java.io.FileOutputStream;

public class SDGtoDot {
    /*
		DotGraph ICFGinDot = new DotGraph("CGinDot");
		Iterator<Unit> itr = ug.iterator();
		while (itr.hasNext()){
			Unit tmp = itr.next();
			if (tmp.branches()){
				System.out.println(tmp);
				List<Unit> succ = ICFG.getSuccsOf(tmp);
				System.out.println(succ);

				ICFGinDot.drawNode(tmp.toString());
				for (Unit u :succ) {
					ICFGinDot.drawNode(u.toString());
					ICFGinDot.drawEdge(tmp.toString(),u.toString());
				}
			}
			else if (ICFG.isCallStmt(tmp)){
				System.out.println(ICFG.getCalleesOfCallAt(tmp));
				//get target method of call Statement
				SootMethod trg = ICFG.getCalleesOfCallAt(tmp).iterator().next();
				DirectedGraph targetMethod = ICFG.getOrCreateUnitGraph(trg);
				HashMutablePDG dummyMainPDG = new HashMutablePDG ((UnitGraph) targetMethod);

				//UnitGraph of target method can be extracted from its active body and pass to PDG
			}
			else if (ICFG.isReturnSite(tmp)){
				//id of call and return statements should be taken into consideration
				System.out.println(ICFG.getCallersOf(ICFG.getMethodOf(tmp)));
			}

		}
	/*	for (;itr.hasNext();itr.next()){
			//ICFGinDot.drawNode(itr.next().toString());
			System.out.println(itr.next().toString());
		}*/

		/*Iterator<soot.jimple.toolkits.callgraph.Edge> CGEdge= Scene.v().getCallGraph().iterator();
		for (;CGEdge.hasNext();CGEdge.next()){
			CGinDot.drawEdge("Class "+CGEdge.next().getSrc().getClass().getName().toString()+": Method "+CGEdge.next().getSrc().method().getName().toString()
					,"Class "+CGEdge.next().getTgt().getClass().getName().toString()+": Method "+CGEdge.next().getSrc().method().getName().toString());
		}*/
		/*try {
			ICFGinDot.render(myfile,32);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
/*
    FileOutputStream myfile ;
    File dotFile = new File("/opt/dotFile.dot");
    myfile = new FileOutputStream(dotFile);
*/
}

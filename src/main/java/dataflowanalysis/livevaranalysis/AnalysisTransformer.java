package dataflowanalysis.livevaranalysis;

import soot.*;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;

import java.util.Map;

public class AnalysisTransformer extends BodyTransformer {

    @Override
    protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
        SootMethod sMethod = Scene.v().getMainMethod();
        UnitGraph graph = new ClassicCompleteUnitGraph(sMethod.getActiveBody());

        LiveVariableAnalysis analysis = new LiveVariableAnalysis(graph);
        // Print live variables at the entry and exit of each node
        for (Unit s : graph) {
            System.out.print(s);
            int d = 40 - s.toString().length();
            while (d > 0) {
                System.out.print(".");
                d--;
            }
            FlowSet<Local> set = analysis.getFlowBefore(s);
            System.out.print("\t[entry: ");
            for (Local local : set) {
                System.out.print(local + " ");
            }
            set = analysis.getFlowAfter(s);
            System.out.print("]\t[exit: ");
            for (Local local : set) {
                System.out.print(local + " ");
            }
            System.out.println("]");
        }
    }
}
package dataflowanalysis.livevaranalysis;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

public class LiveVariableAnalysis extends BackwardFlowAnalysis<Unit, FlowSet<Local>> {

    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     *
     * @param graph
     */
    public LiveVariableAnalysis(DirectedGraph<Unit> graph) {
        super(graph);
        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<Local> in, Unit d, FlowSet<Local> out) {
        FlowSet<Local> kills = new ArraySparseSet<>();
        for (ValueBox def : d.getDefBoxes()) {
            Value value = def.getValue();
            if (value instanceof Local) {
                kills.add((Local) value);
            }
        }
        in.difference(kills, out);
        for (ValueBox use : d.getUseBoxes()) {
            Value value = use.getValue();
            if (value instanceof Local) {
                out.add((Local) value);
            }
        }

    }

    @Override
    protected FlowSet<Local> newInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected void merge(FlowSet<Local> in1, FlowSet<Local> in2, FlowSet<Local> out) {
        in1.union(in2, out);

    }

    @Override
    protected void copy(FlowSet<Local> source, FlowSet<Local> dest) {
        source.copy(dest);
    }
}

package soot;


import org.junit.Test;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class DrawCFG {
    private DotGraph dot = new DotGraph("callgraph");
    private static HashMap<String, Boolean> visited = new
            HashMap<String, Boolean>();


    @Test
    public void test() {
        String classDir = "D:\\work\\graduate\\demo\\target\\classes";
        String className = "edu.tsinghua.demo.controller.DemoController1";

        soot.G.reset();

        Options.v().set_process_dir(Collections.singletonList(classDir));
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_src_prec(Options.src_prec_only_class);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_no_writeout_body_releasing(true);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("cg", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "verbose:false");

        Scene.v().loadNecessaryClasses();

        SootClass entryClass1 = Scene.v().loadClassAndSupport(className);
        SootMethod methodTest1 = entryClass1.getMethodByName("main");

        Options.v().set_main_class(methodTest1.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(methodTest1));


        PackManager.v().runPacks();
        PackManager.v().writeOutput();


        CallGraph callGraph = Scene.v().getCallGraph();

        drawCallGraph(callGraph, "test1.dot");
    }

    @Test
    public void test2() {
        String classDir = "D:\\work\\graduate\\demo\\target\\classes";
        String className = "edu.tsinghua.demo.controller.DemoController3";

        soot.G.reset();

        Options.v().set_process_dir(Collections.singletonList(classDir));
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_src_prec(Options.src_prec_only_class);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_no_writeout_body_releasing(true);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("cg", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "verbose:false");

        Scene.v().loadNecessaryClasses();

        SootClass entryClass1 = Scene.v().loadClassAndSupport(className);
        SootMethod methodTest1 = entryClass1.getMethodByName("main");

        Options.v().set_main_class(methodTest1.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(methodTest1));


        PackManager.v().runPacks();
        PackManager.v().writeOutput();

        CallGraph callGraph = Scene.v().getCallGraph();

        drawCallGraph(callGraph, "test3.dot");
        drawMethodDependenceGraph(methodTest1);
        drawProcedureDependenceGraph(methodTest1);
    }

    private void drawMethodDependenceGraph(SootMethod entryMethod) {
        Body body = entryMethod.retrieveActiveBody();
        ExceptionalUnitGraph exceptionalUnitGraph = new ExceptionalUnitGraph(body);

        CFGToDotGraph cfgForMethod = new CFGToDotGraph();
        DotGraph cfgDot = cfgForMethod.drawCFG(exceptionalUnitGraph, body);
        cfgDot.plot("./cfg.dot");
    }

    private void drawProcedureDependenceGraph(SootMethod entryMethod) {
        Body body = entryMethod.retrieveActiveBody();
        ExceptionalUnitGraph exceptionalUnitGraph = new ExceptionalUnitGraph(body);
        HashMutablePDG hashMutablePDG = new HashMutablePDG(exceptionalUnitGraph);
        CFGToDotGraph pdgForMethod = new CFGToDotGraph();
        DotGraph pdgDot = pdgForMethod.drawCFG(hashMutablePDG, body);
        pdgDot.plot("./pdg.dot");
    }

    private void drawCallGraph(CallGraph callGraph,String name) {
        DotGraph dot = new DotGraph("callgraph");
        Iterator<Edge> iteratorEdges = callGraph.iterator();

        int i = 0;
        System.out.println("Call Graph size : " + callGraph.size());
        while (iteratorEdges.hasNext()) {
            Edge edge = iteratorEdges.next();
            String node_src = edge.getSrc().toString();
            String node_tgt = edge.getTgt().toString();
            if(!node_src.startsWith("<edu.tsinghua")){
                continue;
            }
            dot.drawEdge(node_src, node_tgt);
            System.out.println(i++);
        }

        dot.plot(name);
    }


//    private static void visit(CallGraph cg, SootMethod k) {
//        String identifier = k.getName();
//
//        visited.put(k.getSignature(), true);
//
//
//        dot.drawNode(identifier);
//
//
//        //iterate over unvisited parents
//        Iterator<MethodOrMethodContext> ptargets = new Targets(cg.edgesInto(k));
//
//
//        if (ptargets != null) {
//            while (ptargets.hasNext()) {
//                SootMethod p = (SootMethod) ptargets.next();
//
//
//                if (p == null) System.out.println("p is null");
//
//
//                if (!visited.containsKey(p.getSignature()))
//                    visit(cg, p);
//            }
//        }
//
//
//        //iterate over unvisited children
//        Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(k));
//
//
//        if (ctargets != null) {
//            while (ctargets.hasNext()) {
//                SootMethod c = (SootMethod) ctargets.next();
//                if (c == null) System.out.println("c is null");
//                dot.drawEdge(identifier, c.getName());
//
//
//                if (!visited.containsKey(c.getSignature()))
//                    visit(cg, c);
//            }
//        }
//    }
}

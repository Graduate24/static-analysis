package soot;

import org.junit.Test;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SootTest {

    @Test
    public void test1() {
        G.reset();
        String userDir = System.getProperty("user.dir");
        // target classes
        String classDir = "D:\\work\\graduate\\demo\\target\\classes";
        String className = "edu.tsinghua.demo.controller.DemoController1";

        Options.v().set_process_dir(Collections.singletonList(classDir));
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_src_prec(Options.src_prec_only_class);
        Options.v().set_output_format(Options.output_format_jimple);//输出jimple文件
        Options.v().set_no_writeout_body_releasing(true);
        // Set Soot classpath and other necessary options
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("cg", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "enabled:true");

        SootClass sClass = Scene.v().loadClassAndSupport(className);
        sClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        SootMethod methodTest1 = sClass.getMethodByName("test1");
        methodTest1.retrieveActiveBody();
        Body test1ActiveBody = methodTest1.getActiveBody();

        SootMethod methodTest2 = sClass.getMethodByName("test2");
        methodTest2.retrieveActiveBody();
        Body test2ActiveBody = methodTest2.getActiveBody();

        SootMethod methodTest3 = sClass.getMethodByName("test3");
        methodTest3.retrieveActiveBody();
        Body test3ActiveBody = methodTest2.getActiveBody();

        List<SootMethod> entryPoints = new ArrayList<>();
        entryPoints.add(methodTest1);
        entryPoints.add(methodTest2);
        entryPoints.add(methodTest3);

        Scene.v().setEntryPoints(entryPoints);

        // Load the classes and build the call graph
        PackManager.v().runPacks();
        PackManager.v().writeOutput();

        System.out.println("test1 body: ");
        print(test1ActiveBody);
        System.out.println();
        System.out.println("test2 body: ");
        print(test2ActiveBody);

    }

    private void print(Body body){
        System.out.println("=============CFG====================");
        UnitGraph g = new BriefUnitGraph(body);
        System.out.println(g);
        System.out.println("=============CFG END=============");
        System.out.println();


        System.out.println("=============CG====================");
        CallGraph cg = Scene.v().getCallGraph();
        for (Edge edge : cg) {
            SootMethod srcMethod = edge.src();
            SootMethod tgtMethod = edge.tgt();
            // Print the source and target methods
            System.out.println("Source Method: " + srcMethod);
            System.out.println("Target Method: " + tgtMethod);
            System.out.println();

        }
        System.out.println("=============CG END====================");
        System.out.println();
//
//
        System.out.println("=============Points-To====================");
        PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
        // Example 1: Getting reaching objects of a local variable
        for (Local local : body.getLocals()) {
            // Process each Local variable as needed
            System.out.println("----Local variable: " + local);
            PointsToSet reachingObjects = pointsToAnalysis.reachingObjects(local);
            System.out.println(reachingObjects);
            System.out.println("-------");
            System.out.println();
        }
        System.out.println("=============Points-To END====================");
    }

}

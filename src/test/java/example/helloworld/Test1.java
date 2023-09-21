package example.helloworld;

import org.junit.Before;
import org.junit.Test;
import soot.*;
import soot.jimple.JimpleBody;
import soot.options.Options;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import visual.Visualizer;

import java.io.File;
import java.io.IOException;

public class Test1 {
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes";
    public static String clsName = "example.helloworld.Test";
    public static String methodName = "foo";

    public boolean drawGraph = true;

    @Before
    public void setupSoot() {
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(sourceDirectory);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_no_writeout_body_releasing(true);
        SootClass sc = Scene.v().loadClassAndSupport(clsName);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        PackManager.v().writeOutput();
    }

    @Test
    public void test1() throws InterruptedException, IOException {
        SootClass mainClass = Scene.v().getSootClass(clsName);
        SootMethod sm = mainClass.getMethodByName(methodName);
        JimpleBody body = (JimpleBody) sm.retrieveActiveBody();


        // Print some information about printFizzBuzz
        System.out.println("Method Signature: " + sm.getSignature());
        System.out.println("--------------");
        System.out.println("Argument(s):");
        for (Local l : body.getParameterLocals()) {
            System.out.println(l.getName() + " : " + l.getType());
        }
        System.out.println("--------------");
        //System.out.println("This: " + body.getThisLocal());
        System.out.println("--------------");
        System.out.println("Units:");
        int c = 1;
        for (Unit u : body.getUnits()) {
            System.out.println("(" + c + ") " + u.toString()+" | "+u.getClass());
            c++;
        }
        System.out.println("--------------");
        System.out.println("getFirstNonIdentityStmt:");
        System.out.println(body.getFirstNonIdentityStmt());

        // Print statements that have branch conditions
        System.out.println("Locals:");
        for (Local u : body.getLocals()) {
                System.out.println(u.toString());
        }

        // Draw the control-flow graph of the method
        if (drawGraph) {
            UnitGraph ug = new ClassicCompleteUnitGraph(sm.getActiveBody());
            Visualizer.v().addUnitGraph(ug);
            Visualizer.v().write("cfg.png");

        }
    }

}

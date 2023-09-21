package example.test;

import org.junit.Before;
import org.junit.Test;
import soot.*;
import soot.jimple.JimpleBody;
import soot.options.Options;

import java.io.File;
import java.io.IOException;

public class TestUnusedLocal {
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes";
    public static String clsName = "example.tests.UnusedLocal";

    @Before
    public void setupSoot() {
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(sourceDirectory);
        SootClass sc = Scene.v().loadClassAndSupport(clsName);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

    }

    @Test
    public void test1() throws InterruptedException, IOException {
        // Retrieve printFizzBuzz's body
        SootClass mainClass = Scene.v().getSootClass(clsName);
        SootMethod sm = mainClass.getMethodByName("main");
        JimpleBody body = (JimpleBody) sm.retrieveActiveBody();


        // Print some information about printFizzBuzz
        System.out.println("Method Signature: " + sm.getSignature());
        System.out.println("--------------");

        System.out.println("Units:");
        int c = 1;
        for (Unit u : body.getUnits()) {
            System.out.println("(" + c + ") " + u.toString());
            c++;
        }
        System.out.println("--------------");
    }
}

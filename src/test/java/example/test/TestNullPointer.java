package example.test;

import org.junit.Before;
import org.junit.Test;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TestNullPointer {
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes";
    public static String clsName = "example.tests.NullPointerTest";

    @Before
    public void setupSoot() {
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(sourceDirectory);

        Options.v().set_verbose(true);
        // set npc
        PhaseOptions.v().setPhaseOption("jap.npc", "on");
        SootClass sc = Scene.v().loadClassAndSupport(clsName);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        PackManager.v().getPack("jap").add(
                new Transform("jap.myTransform", new BodyTransformer() {

                    @Override
                    protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
                        int c = 1;
                        for (Unit u : b.getUnits()) {
                            System.out.println("(" + c + ") " + u.toString()+" [tag]: "+u.getTags());
                            c++;
                        }
                    }

                }));

    }

    @Test
    public void test1() throws InterruptedException, IOException {
        PackManager.v().runPacks();
    }
}

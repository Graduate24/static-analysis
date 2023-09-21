package dataflowanalysis.livevaranalysis;

import soot.*;
import soot.options.Options;

import java.io.File;

public class LivaVariableMain {
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes";
    public static String clsName = "example.helloworld.LiveVariable";

    public static void setupSoot() {
        G.reset();
        Options.v().set_soot_classpath(sourceDirectory);
        Options.v().set_prepend_classpath(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_keep_offset(true);
        SootClass sc = Scene.v().loadClassAndSupport(clsName);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();
    }

    public static void main(String[] args) {
        setupSoot();
        // Add transformer to appropriate Pack in PackManager. PackManager will run all Packs when main function of Soot is called
        PackManager.v().getPack("jtp").add(new Transform("jtp.lva", new AnalysisTransformer()));
        PackManager.v().runPacks();
    }
}

package example.basic;

import org.junit.Before;
import org.junit.Test;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.scalar.Pair;
import tools.visual.Visualizer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

public class BasicTest {
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes";
    public static String circleClassName = "example.basic.Circle";

    @Before
    public void setupSoot() {
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(sourceDirectory);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        Options.v().set_whole_program(true);
        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();

    }

    @Test
    public void test1() throws InterruptedException, IOException {
        // Access to Classes
        SootClass circleClass = reportSootClassInfo();
        // Access to Fields
        SootField radiusField = reportSootFieldInfo(circleClass);
        // Access to Methods
        SootMethod areaMethod = reportSootMethodInfo(circleClass);
        // Access to Body (units, locals)
        System.out.println("-----Body-----");
        JimpleBody body = (JimpleBody) areaMethod.getActiveBody();
        reportLocalInfo(body);

        Stmt firstNonIdentitiyStmt = body.getFirstNonIdentityStmt();
        int c = 0;
        for (Unit u : body.getUnits()) {
            c++;
            Stmt stmt = (Stmt) u;
            System.out.println(String.format("(%d): %s", c, stmt));
            if (stmt.equals(firstNonIdentitiyStmt))
                System.out.println("    This statement is the first non-identity statement!");
            if (stmt.containsFieldRef())
                reportFieldRefInfo(radiusField, stmt);
            if (doesInvokeMethod(stmt, "int area()", circleClassName)) {
                System.out.println("    This statement invokes 'int area()' method");
            }
            modifyBody(body, stmt);
        }
        for (Trap trap : body.getTraps()) {
            System.out.println(trap);
        }

        try {
            body.validate();
            System.out.println("Body is validated! No inconsistency found.");
        } catch (Exception exception) {
            System.out.println("Body is not validated!");
        }
        // Call graph
        System.out.println("-----CallGraph-----");
        CallGraph callGraph = Scene.v().getCallGraph();
        for (Iterator<Edge> it = callGraph.edgesOutOf(areaMethod); it.hasNext(); ) {
            Edge edge = it.next();
            System.out.println(String.format("Method '%s' invokes method '%s' through stmt '%s", edge.src(), edge.tgt(), edge.srcUnit()));
        }
//            Visualizer.v().addCallGraph(callGraph);
        Visualizer.v().addCallGraph(callGraph,
                edge -> edge.src().getDeclaringClass().isApplicationClass(),
                sootMethod -> new Pair<>(
                        sootMethod.getDeclaringClass().isApplicationClass()
                                ? "cg_node, default_color" : "cg_node, cg_lib_class"
                        , sootMethod.getDeclaringClass().isApplicationClass()
                        ? sootMethod.getSubSignature() : sootMethod.getSignature())
        );
        Visualizer.v().write("cg.png");
//        Visualizer.v().draw();
//        TimeUnit.SECONDS.sleep(60);
    }

    private SootClass reportSootClassInfo() {
        System.out.println("-----Class-----");
        SootClass circleClass = Scene.v().getSootClass(circleClassName);
        System.out.println(String.format("The class %s is an %s class, loaded with %d methods! ",
                circleClass.getName(), circleClass.isApplicationClass() ? "Application" : "Library", circleClass.getMethodCount()));
        String wrongClassName = "Circrle";
        SootClass notExistedClass = Scene.v().getSootClassUnsafe(wrongClassName, false);
        System.out.println(String.format("getClassUnsafe: Is the class %s null? %b", wrongClassName, notExistedClass == null));
        try {
            notExistedClass = Scene.v().getSootClass(wrongClassName);
            System.out.println(String.format("getClass creates a phantom class for %s: %b", wrongClassName, notExistedClass.isPhantom()));
        } catch (Exception exception) {
            System.out.println(String.format("getClass throws an exception for class %s.", wrongClassName));
        }
        Type circleType = circleClass.getType();
        System.out.printf("Class '%s' is same as class of type '%s': %b%n"
                , circleClassName, circleType.toString(), circleClass.equals(Scene.v().getSootClass(circleType.toString())));
        return circleClass;
    }

    private SootField reportSootFieldInfo(SootClass circleClass) {
        SootField radiusField = circleClass.getField("radius", IntType.v());
        SootField piField = circleClass.getField("double PI");
        System.out.printf("Field %s is final: %b%n", piField, piField.isFinal());
        return radiusField;
    }

    private SootMethod reportSootMethodInfo(SootClass circleClass) {
        System.out.println("-----Method-----");
        System.out.println(String.format("List of %s's methods:", circleClass.getName()));
        for (SootMethod sootMethod : circleClass.getMethods())
            System.out.println(String.format("- %s", sootMethod.getName()));
        SootMethod getCircleCountMethod = circleClass.getMethod("int getCircleCount()");
        System.out.println(String.format("Method Signature: %s", getCircleCountMethod.getSignature()));
        System.out.println(String.format("Method Subsignature: %s", getCircleCountMethod.getSubSignature()));
        System.out.println(String.format("Method Name: %s", getCircleCountMethod.getName()));
        System.out.println(String.format("Declaring class: %s", getCircleCountMethod.getDeclaringClass()));
        int methodModifers = getCircleCountMethod.getModifiers();
        System.out.println(String.format("Method %s is public: %b, is static: %b, is final: %b", getCircleCountMethod.getName(),
                Modifier.isPublic(methodModifers),
                Modifier.isStatic(methodModifers),
                Modifier.isFinal(methodModifers)));
        SootMethod constructorMethod = circleClass.getMethodByName("<init>");
        try {
            SootMethod areaMethod = circleClass.getMethodByName("area");
        } catch (Exception exception) {
            System.out.println("Th method 'area' is overloaded and Soot cannot retrieve it by name");
        }
        return circleClass.getMethod("int area(boolean)");
    }

    private void reportLocalInfo(JimpleBody body) {
        System.out.println(String.format("Local variables count: %d", body.getLocalCount()));
        Local thisLocal = body.getThisLocal();
        Type thisType = thisLocal.getType();
        Local paramLocal = body.getParameterLocal(0);
    }

    private void reportFieldRefInfo(SootField radiusField, Stmt stmt) {
        FieldRef fieldRef = stmt.getFieldRef();
        fieldRef.apply(new AbstractRefSwitch() {
            @Override
            public void caseStaticFieldRef(StaticFieldRef v) {
                // A static field reference
            }

            @Override
            public void caseInstanceFieldRef(InstanceFieldRef v) {
                if (v.getField().equals(radiusField)) {
                    System.out.println(String.format("    Field %s is used through FieldRef '%s'. The base local of FieldRef has type '%s'", radiusField, v, v.getBase().getType()));
                }
            }
        });
    }

    private void modifyBody(JimpleBody body, Stmt stmt) {
        stmt.apply(new AbstractStmtSwitch() {
            @Override
            public void caseIfStmt(IfStmt stmt) {
                System.out.println(String.format("    (Before change) if condition '%s' is true goes to stmt '%s'", stmt.getCondition(), stmt.getTarget()));
                stmt.setTarget(body.getUnits().getSuccOf(stmt));
                System.out.println(String.format("    (After change) if condition '%s' is true goes to stmt '%s'", stmt.getCondition(), stmt.getTarget()));
            }
        });
    }

    private boolean doesInvokeMethod(Stmt stmt, String subsignature, String declaringClass) {
        if(!stmt.containsInvokeExpr())
            return false;
        InvokeExpr invokeExpr = stmt.getInvokeExpr();
        invokeExpr.apply(new AbstractJimpleValueSwitch() {
            @Override
            public void caseStaticInvokeExpr(StaticInvokeExpr v) {
                System.out.println(String.format("    StaticInvokeExpr '%s' from class '%s'", v, v.getType()));
            }

            @Override
            public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
                System.out.println(String.format("    VirtualInvokeExpr '%s' from local '%s' with type %s", v, v.getBase(), v.getBase().getType()));
            }

            @Override
            public void defaultCase(Object v) {
                super.defaultCase(v);
            }
        });
        return invokeExpr.getMethod().getSubSignature().equals(subsignature)
                && invokeExpr.getMethod().getDeclaringClass().getName().equals(declaringClass);
    }
}

package taintanalysis.taintWrappers;


import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

public class DynamicInvokeTaintWrapper extends EasyTaintWrapper {

    public DynamicInvokeTaintWrapper(File f) throws IOException {
        super(f);
    }

    public DynamicInvokeTaintWrapper(InputStream stream) throws IOException {
        super(stream);
    }

    @Override
    public Set<AccessPath> getTaintsForMethodInternal(Stmt stmt, AccessPath taintedPath) {
        if (!stmt.containsInvokeExpr())
            return Collections.singleton(taintedPath);

        final Set<AccessPath> taints = super.getTaintsForMethodInternal(stmt, taintedPath);

        // if stmt invokes StringConcatFactory bootstrap methods,
        // taints in parameter should be propagated to the
        // left side of assignment
        boolean isSupported = stmt.getInvokeExpr() instanceof DynamicInvokeExpr
                && stmt instanceof DefinitionStmt
                && ((DynamicInvokeExpr)stmt.getInvokeExpr()).getBootstrapMethodRef()
                .getDeclaringClass().getName().equals("java.lang.invoke.StringConcatFactory");

        if (!isSupported)
            return taints;

        // "if we are inside a conditional, we always taint" -- EasyTaintWrapper.java
        // not sure what is the conditional
        boolean doTaint = taintedPath.isEmpty();

        // check whether any parameter is tainted
        if (!doTaint) {
            for (Value param : stmt.getInvokeExpr().getArgs()) {
                if (param.equals(taintedPath.getPlainValue())) {
                    doTaint = true;
                    break;
                }
            }
        }

        if (doTaint) {
            taints.add(manager.getAccessPathFactory()
                    .createAccessPath(((DefinitionStmt) stmt).getLeftOp(), true));
        }

        return taints;
    }
}

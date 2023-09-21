package taintanalysis.sourcesink.manager;

import soot.SootMethod;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowManager;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.sourcesSinks.manager.DefaultSourceSinkManager;
import soot.jimple.infoflow.sourcesSinks.manager.SourceInfo;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;
import taintanalysis.sourcesink.definitions.ParamSourceSinkDefinition;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class ParamSourceSinkManager extends DefaultSourceSinkManager {

    protected final HashSet<String> taintParamTags = new HashSet<>(
            Arrays.asList("Lorg/springframework/web/bind/annotation/RequestParam;",
                    "Lorg/springframework/web/bind/annotation/PathVariable;"));

    public ParamSourceSinkManager(Collection<String> sources, Collection<String> sinks) {
        super(sources, sinks);
    }

    @Override
    public SourceInfo getSourceInfo(Stmt sCallSite, InfoflowManager manager) {
        SourceInfo source = super.getSourceInfo(sCallSite, manager);
        // if the call is a source, default source sink manager will return its information
        if(source != null) {
            return source;
        }
        AccessPath targetAp = null;
        if (!(sCallSite instanceof IdentityStmt))
            return null;
        IdentityStmt istmt = (IdentityStmt) sCallSite;
        if (!(istmt.getRightOp() instanceof ParameterRef))
            return null;
        ParameterRef pref = (ParameterRef) istmt.getRightOp();
        SootMethod currentMethod = manager.getICFG().getMethodOf(istmt);
        VisibilityParameterAnnotationTag paramTags = (VisibilityParameterAnnotationTag) currentMethod.getTag("VisibilityParameterAnnotationTag");
        if(paramTags == null)
            return null;
        VisibilityAnnotationTag tag = paramTags.getVisibilityAnnotations().get(pref.getIndex());
        AnnotationTag targetTag = null;
        if (tag != null) {
            for (AnnotationTag annotaion: tag.getAnnotations()) {
                if (taintParamTags.contains(annotaion.getType())) {
                    targetTag = annotaion;
                    targetAp = manager.getAccessPathFactory()
                            .createAccessPath(currentMethod.getActiveBody().getParameterLocal(pref.getIndex()), true);
                }
            }
        }
        if (targetAp == null)
            return null;

        // IdentityStmt has no callee
        return new SourceInfo(new ParamSourceSinkDefinition(targetTag), targetAp);
    }
}

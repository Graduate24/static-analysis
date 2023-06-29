package ta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.dexpler.DalvikThrowAnalysis;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.cfg.BiDirICFGFactory;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.OnTheFlyJimpleBasedICFG;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraphFactory;

public class CachedBiDiICFGFactory implements BiDirICFGFactory {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected boolean isAndroid = false;

    private IInfoflowCFG iCFG;

    public IInfoflowCFG getiCFG() {
        return iCFG;
    }

    @Override
    public IInfoflowCFG buildBiDirICFG(InfoflowConfiguration.CallgraphAlgorithm callgraphAlgorithm, boolean enableExceptions) {
        if (callgraphAlgorithm == InfoflowConfiguration.CallgraphAlgorithm.OnDemand) {
            // Load all classes on the classpath to signatures
            long beforeClassLoading = System.nanoTime();
            OnTheFlyJimpleBasedICFG.loadAllClassesOnClassPathToSignatures();
            logger.info("Class loading took {} seconds", (System.nanoTime() - beforeClassLoading) / 1E9);

            long beforeHierarchy = System.nanoTime();
            Scene.v().getOrMakeFastHierarchy();
            assert Scene.v().hasFastHierarchy();
            logger.info("Hierarchy building took {} seconds", (System.nanoTime() - beforeHierarchy) / 1E9);

            long beforeCFG = System.nanoTime();
            IInfoflowCFG cfg = new InfoflowCFG(new OnTheFlyJimpleBasedICFG(Scene.v().getEntryPoints()));
            logger.info("CFG generation took {} seconds", (System.nanoTime() - beforeCFG) / 1E9);

            return cfg;
        }

        BiDiInterproceduralCFG<Unit, SootMethod> baseCFG = getBaseCFG(enableExceptions);

        this.iCFG = new InfoflowCFG(baseCFG);
        return this.iCFG;
    }

    protected BiDiInterproceduralCFG<Unit, SootMethod> getBaseCFG(boolean enableExceptions) {
        // If we are running on Android, we need to use a different throw
        // analysis
        JimpleBasedInterproceduralCFG baseCFG = null;
        if (isAndroid) {
            baseCFG = new JimpleBasedInterproceduralCFG(enableExceptions, true) {

                protected DirectedGraph<Unit> makeGraph(Body body) {
                    return enableExceptions ? ExceptionalUnitGraphFactory.createExceptionalUnitGraph(body, DalvikThrowAnalysis.interproc(), true) : new BriefUnitGraph(body);
                }

            };
        } else baseCFG = new JimpleBasedInterproceduralCFG(enableExceptions, true);
        baseCFG.setIncludePhantomCallees(true);

        return baseCFG;
    }

    /**
     * Sets whether this CFG will be used to analyze Android apps
     *
     * @param isAndroid True if the CFG will be used to analyze Android apps,
     *                  otherwise false
     */
    public void setIsAndroid(boolean isAndroid) {
        this.isAndroid = isAndroid;
    }
}

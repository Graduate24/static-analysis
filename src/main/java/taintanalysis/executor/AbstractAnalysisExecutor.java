package taintanalysis.executor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jspc.compile.JspCompiler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.results.InfoflowResults;
import taintanalysis.config.Config;
import taintanalysis.engine.IFFactory;
import taintanalysis.engine.ReuseableInfoflow;
import taintanalysis.pathchecker.PathCheckResult;
import taintanalysis.pathchecker.PathCheckerManager;
import taintanalysis.phase.*;
import taintanalysis.result.DetectedResult;
import taintanalysis.result.RuleResult;
import taintanalysis.rule.Rule;
import taintanalysis.rule.RuleSelectorManager;
import utils.ClassPathResource;
import utils.PathUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public abstract class AbstractAnalysisExecutor implements
        Preliminary<Config, Config>,
        EngineBuild<Config, Infoflow>,
        Analysis<Infoflow, List<ImmutablePair<Rule, InfoflowResults>>>,
        ResultBuild<List<ImmutablePair<Rule, InfoflowResults>>, List<RuleResult>>,
        Posterior<List<RuleResult>, Void> {

    static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static final ClassPathResource defaultConfig = new ClassPathResource("defaultconfig.json");

    Config config;

    boolean useDefault;

    ReuseableInfoflow infoflow;

    String output = "./result.json";

    List<RuleResult> ruleResult;

    boolean writeOutput;

    boolean trackSourceFile;

    List<Function<InfoflowResults, PathCheckResult>> postChecks;

    boolean doPostCheck = true;

    boolean jspc = false;

    RuleSelectorManager ruleSelectorManager = new RuleSelectorManager();

    public boolean isTrackSourceFile() {
        return trackSourceFile;
    }

    public boolean isJspc() {
        return jspc;
    }

    public Config getConfig() {
        return config;
    }

    public boolean isUseDefault() {
        return useDefault;
    }

    public ReuseableInfoflow getInfoflow() {
        return infoflow;
    }

    public String getOutput() {
        return output;
    }

    public List<RuleResult> getRuleResult() {
        return ruleResult;
    }

    public boolean isWriteOutput() {
        return writeOutput;
    }


    public List<String> showAllRules() {
        return ruleSelectorManager.all().stream().map(Rule::getRuleCwe).collect(toList());
    }


    public abstract AnalysisExecutor analysis();

    @Override
    public Config pre(Config config) {
        if (config == null) {
            throw new AssertionError("config must be set before analysis.");
        }
        if (config.getProject() == null || config.getProject().isEmpty()) {
            throw new AssertionError("project must be set before analysis.");
        } else {
            if (jspc) {
                JspCompiler jspCompiler = new JspCompiler();
                jspCompiler.compile(config.getProject(), config.getProject());

            }
        }
        if (config.getRules() == null || config.getRules().isEmpty()) {
            throw new AssertionError("rules must not be empty.");
        }
        if (config.getPathCheckers() != null && !config.getPathCheckers().isEmpty()) {
            this.postChecks = PathCheckerManager.buildPathCheckerManager().pathCheckerList(config.getPathCheckers());
        }
        config.autoConfig();
        if (!useDefault) {
            List<String> realLibPath = new ArrayList<>();
            for (String path : config.getLibPaths()) {
                if (path.endsWith(".jar")) {
                    realLibPath.add(path);
                } else {
                    File file = new File(path);
                    if (file.isDirectory()) {
                        realLibPath.addAll(Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(f -> f.getName().endsWith(".jar")).map(File::getPath).collect(toList()));
                    }
                }
            }
            config.setLibPath(String.join(File.pathSeparator, realLibPath));
            if (config.getJdk() != null && !config.getJdk().isEmpty()) {
                config.setLibPath(config.getLibPath() + File.pathSeparator + config.getJdk());
            }
        }

        return config;
    }

    @Override
    public List<ImmutablePair<Rule, InfoflowResults>> doAnalysis(Infoflow infoflow) {
        ReuseableInfoflow reuseableInfoflow = (ReuseableInfoflow) infoflow;
        Config analysisConfig = reuseableInfoflow.getAnalysisConfig();
        String appPath = analysisConfig.getAppPath();
        List<String> entryPoints = analysisConfig.getEpoints();
        String libPath = analysisConfig.getLibPath();
        List<ImmutablePair<Rule, InfoflowResults>> ruleResult = new ArrayList<>();
        for (Rule r : analysisConfig.getRules()) {
            reuseableInfoflow.computeInfoflow(appPath, libPath, entryPoints, r.getSources(), r.getSinks());
            ruleResult.add(ImmutablePair.of(r, reuseableInfoflow.getResults()));
        }
        return ruleResult;
    }

    @Override
    public Infoflow buildEngine(Config config) {
        String appPath = config.getAppPath();
        List<String> excludes = new ArrayList<>(config.getExcludes());
        int timeout = config.getPathReconstructionTimeout();
        String callGraphAlgorithm = config.getCallgraphAlgorithm();
        ReuseableInfoflow reuseableInfoflow = IFFactory.buildReusable(appPath, excludes, timeout, callGraphAlgorithm);
        reuseableInfoflow.setAnalysisConfig(config);
        this.infoflow = reuseableInfoflow;
        return reuseableInfoflow;
    }

    @Override
    public Void post(List<RuleResult> unused) {
        PathUtil.deteleTempdir(config.getTempDir());
        return null;
    }

    @Override
    public List<RuleResult> buildResult(List<ImmutablePair<Rule, InfoflowResults>> ruleResults) {
        List<RuleResult> ruleResultList = new ArrayList<>();
        for (ImmutablePair<Rule, InfoflowResults> result : ruleResults) {
            if (doPostCheck && postChecks != null) {
                postChecks.forEach(f -> f.apply(result.getRight()));
            }
            List<DetectedResult> results = PathUtil.detectedResults(result.getRight(), infoflow.getICFG(), config.getProject(), trackSourceFile);
            ruleResultList.add(new RuleResult(result.getLeft().getName(), result.getLeft().getRuleCwe(), result.getLeft().getRuleLevel(), results));
        }
        this.ruleResult = ruleResultList;
        if (writeOutput) {
            try {
                String json = gson.toJson(ruleResultList);
                Writer writer = new FileWriter(output);
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ruleResultList;
    }

}

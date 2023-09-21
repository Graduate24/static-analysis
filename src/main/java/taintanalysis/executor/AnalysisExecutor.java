package taintanalysis.executor;

import org.apache.commons.lang3.tuple.ImmutablePair;
import soot.jimple.infoflow.results.InfoflowResults;
import taintanalysis.config.Config;
import taintanalysis.engine.ReuseableInfoflow;
import taintanalysis.result.RuleResult;
import taintanalysis.rule.Rule;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class AnalysisExecutor extends AbstractAnalysisExecutor {


    public static AnalysisExecutor newInstance() {
        AnalysisExecutor analysisExecutor = new AnalysisExecutor();
        return analysisExecutor.withDefaultConfig();
    }

    public AnalysisExecutor withDefaultConfig() {
        try {
            this.config = gson.fromJson(new InputStreamReader(defaultConfig.getInputStream()), Config.class);
            this.useDefault = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public AnalysisExecutor setRules(String... cweId) {
        List<Rule> rules = ruleSelectorManager.select(cweId);
        if (rules.isEmpty()) {
            throw new RuntimeException("No rules found.");
        }
        this.config.setRules(rules);
        return this;
    }

    public AnalysisExecutor setRules(List<String> cweIds) {
        return setRules(cweIds.toArray(new String[0]));
    }


    public AnalysisExecutor withAllRules() {
        List<Rule> rules = ruleSelectorManager.all();
        if (rules.isEmpty()) {
            throw new RuntimeException("No rules found.");
        }
        this.config.setRules(rules);
        return this;
    }

    public AnalysisExecutor withConfig(String configFilePath) {
        try {
            this.config = gson.fromJson(new FileReader(configFilePath), Config.class);
            this.useDefault = false;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public AnalysisExecutor withConfig(Config config) {
        this.config = config;
        return this;
    }


    public AnalysisExecutor setProject(String project) {
        this.config.setProject(project);
        return this;
    }

    public AnalysisExecutor setJDK(String jdk) {
        this.config.setJdk(jdk);
        return this;
    }

    public AnalysisExecutor setOutput(String output) {
        this.output = output;
        return this;
    }

    public AnalysisExecutor setTimeout(int timeout) {
        this.config.setPathReconstructionTimeout(timeout);
        return this;
    }

    public AnalysisExecutor writeOutput(boolean writeOutput) {
        this.writeOutput = writeOutput;
        return this;
    }

    public AnalysisExecutor setCallGraphAlgorithm(String callGraphAlgorithm) {
        this.config.setCallgraphAlgorithm(callGraphAlgorithm);
        return this;
    }

    public AnalysisExecutor trackSourceFile(boolean trackSourceFile) {
        this.trackSourceFile = trackSourceFile;
        return this;
    }

    public AnalysisExecutor setEntrySelector(String entrySelector) {
        this.config.setEntrySelector(entrySelector);
        return this;
    }

    public AnalysisExecutor setPathChecker(String pathChecker) {
        this.config.setPathCheckers(pathChecker);
        return this;
    }

    public AnalysisExecutor setJspc(boolean jspc) {
        this.jspc = jspc;
        return this;
    }


    @Override
    public AnalysisExecutor analysis() {
        Config analysisConfig = pre(config);
        ReuseableInfoflow reuseableInfoflow = (ReuseableInfoflow) buildEngine(analysisConfig);
        List<ImmutablePair<Rule, InfoflowResults>> pairList = doAnalysis(reuseableInfoflow);
        List<RuleResult> ruleResult = buildResult(pairList);
        post(ruleResult);
        return this;
    }
}

package core;

import org.junit.Test;
import taintanalysis.executor.AnalysisExecutor;
import taintanalysis.result.RuleResult;

import java.util.List;

public class AnalysisExecutorTest {

    @Test
    public void test1() {
        // Project to be analysis. Can be directory path, .jar file or .zip file path.
        String project = "/home/ran/Documents/work/thusa2/ifpc-testcase/WebGoat-5.0";
        // jdk path for the project. can be omitted if configuration file contains it or "libPath" of config includes it.
        String jdk = "/home/ran/Documents/work/thusa2/ifpc-testcase/jdk/rt.jar";
        String result = "test_result.json";
        AnalysisExecutor analysisExecutor = AnalysisExecutor
                .newInstance()
                .withDefaultConfig()
                .setProject(project)
                .setJDK(jdk)
                // default is "SPARK"
                .setCallGraphAlgorithm("SPARK")
                // Track source file and calculate line number of jsp. Default false.
                .trackSourceFile(true)
                // Path reconstruction time out.
                .setTimeout(180)
                // write detect result to file.
                .writeOutput(true)
                .setOutput(result)
                .analysis();

        // detect result.
        List<RuleResult> ruleResult = analysisExecutor.getRuleResult();
        System.out.println(ruleResult);
    }

    @Test
    public void test2() {
        // User defined config.
        String configPath = "config4.json";
        String result = "test_result.json";
        AnalysisExecutor analysisExecutor = AnalysisExecutor
                .newInstance()
                .withConfig(configPath)
                .setCallGraphAlgorithm("SPARK")
                // Track source file and calculate line number of jsp. Default false.
                .trackSourceFile(true)
                .writeOutput(true)
                .setOutput(result)
                .analysis();

        // detect result.
        List<RuleResult> ruleResult = analysisExecutor.getRuleResult();
        System.out.println(ruleResult);
    }

    @Test
    public void test3() {
        // TODO Make entry point selector configurable.
    }

    @Test
    public void test4() {
        // test WebGoat
        String project = "/home/ran/Documents/work/thusa2/testprojects/WebGoat-5.0";
        String jdk = "jdk/rt.jar";
        String result = "result/webgoat_result.json";
        AnalysisExecutor analysisExecutor = AnalysisExecutor
                .newInstance()
                .withDefaultConfig()
                .setProject(project)
                .setJDK(jdk)
                // default is "SPARK"
                .setCallGraphAlgorithm("SPARK")
                // Track source file and calculate line number of jsp. Default false.
                .trackSourceFile(true)
                // Path reconstruction time out.
                .setTimeout(180)
                // write detect result to file.
                .writeOutput(true)
                .setOutput(result)
                .analysis();

    }

    @Test
    public void test5() {
        // test jsp
        String project = "/home/ran/Documents/work/thusa2/jsp-demo2_uncomplie";
        String jdk = "jdk/rt.jar";
        String result = "result/jsp_result.json";
        AnalysisExecutor analysisExecutor = AnalysisExecutor
                .newInstance()
                .withDefaultConfig()
                .setProject(project)
                .setJDK(jdk)
                // jsp compiler
                .setJspc(true)
                // default is "SPARK"
                .setCallGraphAlgorithm("SPARK")
                // Track source file and calculate line number of jsp. Default false.
                .trackSourceFile(true)
                // Path reconstruction time out.
                .setTimeout(180)
                // write detect result to file.
                .writeOutput(true)
                .setOutput(result)
                .analysis();
    }

    @Test
    public void test6() {
        // test zip
        String project = "/home/ran/Documents/work/thusa2/testprojects/jsp-demo.zip";
        String jdk = "jdk/rt.jar";
        String result = "result/zip_result.json";
        AnalysisExecutor analysisExecutor = AnalysisExecutor
                .newInstance()
                .withDefaultConfig()
                .setProject(project)
                .setJDK(jdk)
                // default is "SPARK"
                .setCallGraphAlgorithm("SPARK")
                // Track source file and calculate line number of jsp. Default false.
                .trackSourceFile(true)
                // Path reconstruction time out.
                .setTimeout(180)
                // write detect result to file.
                .writeOutput(true)
                .setOutput(result)
                .analysis();
    }

    @Test
    public void test7() {
        // test jar
        String project = "/home/ran/Documents/work/thusa2/testprojects/rm-broker.jar";
        String jdk = "jdk/rt.jar";
        String result = "result/jar_result.json";
        AnalysisExecutor analysisExecutor = AnalysisExecutor
                .newInstance()
                .withDefaultConfig()
                .setProject(project)
                .setJDK(jdk)
                // default is "SPARK"
                .setCallGraphAlgorithm("CHA")
                // Track source file and calculate line number of jsp. Default false.
                .trackSourceFile(true)
                // Path reconstruction time out.
                .setTimeout(180)
                // write detect result to file.
                .writeOutput(true)
                .setOutput(result)
                // Entry selectors:JspServiceEntry,AnnotationTagEntry,PublicStaticOrMainEntry
                .setEntrySelector("AnnotationTagEntry,PublicStaticOrMainEntry")
                .analysis();
    }

    @Test
    public void test8() {
        // cmd
        // /home/ran/.jdks/graalvm-ce-17/bin/java -jar ta.jar -dc true -p "/home/ran/Documents/work/thusa2/ifpc-testcase/WebGoat-5.0" -j "/home/ran/Documents/work/thusa2/ifpc-testcase/jdk/rt.jar" -t true -w true -o result.json -cg SPARK -to 180
    }


    @Test
    public void test9() {
        // test WebGoat
        String project = "/home/ran/Documents/work/thusa2/testprojects/WebGoat-5.0";
        String jdk = "jdk/rt.jar";
        String result = "result/webgoat_result.json";
        AnalysisExecutor analysisExecutor = AnalysisExecutor
                .newInstance()
                .withDefaultConfig()
                .setProject(project)
                .setJDK(jdk)
                // set rule by cwe id
                .setRules("22", "78")
                .trackSourceFile(true)
                .writeOutput(true)
                .setOutput(result)
                .analysis();

        // Show all cwe rules: [78, 22, 89]
        System.out.println(AnalysisExecutor.newInstance().withDefaultConfig().showAllRules());
    }

}

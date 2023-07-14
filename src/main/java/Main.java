import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import ta.AnalysisExecutor;
import ta.RuleSelectorManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Main {
    /**
     * usage: ta [-h] [-dc {true,false}] [-c CONFIG] [-p [PROJECT [PROJECT ...]]]
     *           [-j JDK] [-t {true,false}] [-w {true,false}] [-o OUTPUT]
     *           [-cg {CHA,SPARK,VTA,RTA,GEOM}] [-to TIMEOUT]
     *           [-es [ENTRYSELECTOR [ENTRYSELECTOR ...]]]
     *           [-pc [PATHCHECKER [PATHCHECKER ...]]] [-r [RULES [RULES ...]]]
     *           [-lr LISTRULE]
     *
     * Run taint  analysis  of  given  project.  Example:  -dc  true  -p /project1
     * /project2 -j /jdk/rt.jar -t true -w true -o result.json -cg SPARK -to 180 -
     * r 78 22 89
     *
     * named arguments:
     *   -h, --help             show this help message and exit
     *   -dc {true,false}, --defaultconfig {true,false}
     *                          Specify if use default config. (default: true)
     *   -c CONFIG, --config CONFIG
     *                          User defined config file path.
     *   -p [PROJECT [PROJECT ...]], --project [PROJECT [PROJECT ...]]
     *                          Project to be analysis.  Can  be directory path, .
     *                          jar file or .zip file path.
     *   -j JDK, --jdk JDK      Jdk path  for  the  project.  can  be  omitted  if
     *                          configuration file  contains  it  or  "libPath" of
     *                          config includes it.
     *   -t {true,false}, --track {true,false}
     *                          Track source file  and  calculate  line  number of
     *                          jsp files. (default: false)
     *   -w {true,false}, --write {true,false}
     *                          Write detect result to file. (default: true)
     *   -o OUTPUT, --output OUTPUT
     *                          Out put file path.
     *   -cg {CHA,SPARK,VTA,RTA,GEOM}, --callgraph {CHA,SPARK,VTA,RTA,GEOM}
     *                          Call graph algorithm. (default: SPARK)
     *   -to TIMEOUT, --timeout TIMEOUT
     *                          Path reconstruction time out. (default: 180)
     *   -es [ENTRYSELECTOR [ENTRYSELECTOR ...]], --entryselector [ENTRYSELECTOR [ENTRYSELECTOR ...]]
     *                          entry        selectors,         choose        from
     *                          'JspServiceEntry','AnnotationTagEntry','PublicStaticOrMainEntry'.
     *                          Multiple  selectors  can  be  set   with  '  '  in
     *                          between. Default all
     *   -pc [PATHCHECKER [PATHCHECKER ...]], --pathchecker [PATHCHECKER [PATHCHECKER ...]]
     *                          path checkers.  choose  from  'default'.  Multiple
     *                          selectors can be set with ' ' in between.
     *   -r [RULES [RULES ...]], --rules [RULES [RULES ...]]
     *                          rules (cwe id)  for  analysis.  Multiple rules can
     *                          be set with ' '  in  between.  Default all if with
     *                          default config.
     *   -lr LISTRULE, --listrule LISTRULE
     *                          'true' to list rules in current config.
     *
     * @param args
     */
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("ta").build()
                .defaultHelp(true)
                .description("Run taint analysis of given project. Example: -dc true -p /project1 /project2 -j /jdk/rt.jar -t true -w true -o result.json -cg SPARK -to 180 -r 78 22 89");
        parser.addArgument("-dc", "--defaultconfig")
                .choices("true", "false").setDefault("true")
                .help("Specify if use default config.");
        parser.addArgument("-c", "--config")
                .help("User defined config file path.");
        parser.addArgument("-p", "--project").nargs("*")
                .help("Project to be analysis. Can be directory path, .jar file or .zip file path.");
        parser.addArgument("-j", "--jdk")
                .help("Jdk path for the project. can be omitted if configuration file contains it or \"libPath\" of config includes it.");
        parser.addArgument("-t", "--track")
                .choices("true", "false").setDefault("false")
                .help("Track source file and calculate line number of jsp files.");
        parser.addArgument("-w", "--write")
                .choices("true", "false").setDefault("true")
                .help("Write detect result to file.");
        parser.addArgument("-o", "--output")
                .help("Out put file path.");
        parser.addArgument("-cg", "--callgraph")
                .choices("CHA", "SPARK", "VTA", "RTA", "GEOM").setDefault("SPARK")
                .help("Call graph algorithm.");
        parser.addArgument("-to", "--timeout")
                .setDefault(180).help("Path reconstruction time out.");
        parser.addArgument("-es", "--entryselector").nargs("*")
                .help("entry selectors, choose from 'JspServiceEntry','AnnotationTagEntry','PublicStaticOrMainEntry'. Multiple selectors can be set with ' ' in between. Default all");

        parser.addArgument("-pc", "--pathchecker").nargs("*")
                .help("path checkers. choose from 'default'. Multiple selectors can be set with ' ' in between.");

        parser.addArgument("-r", "--rules").nargs("*")
                .help("rules (cwe id) for analysis. Multiple rules can be set with ' ' in between.  Default all if with default config.");

        parser.addArgument("-lr", "--listrule")
                .help("'true' to list rules in current config.");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        AnalysisExecutor analysisExecutor = AnalysisExecutor.newInstance();

        if (ns.getString("defaultconfig").equals("true")) {
            analysisExecutor.withDefaultConfig();
        } else {
            String config = ns.getString("config");
            analysisExecutor.withConfig(config);
        }

        if (ns.getString("listrule").equals("true")) {
            System.out.println("rules in current config:");
            analysisExecutor.getConfig().getRules().forEach(c -> {
                System.out.println("    name: " + c.getName() + ", ruleCwe: " + c.getRuleCwe());
            });
            System.exit(65);
        }


        String jdk = ns.getString("jdk");
        if (jdk != null) {
            analysisExecutor.setJDK(jdk);
        }
        String track = ns.getString("track");
        if (track != null) {
            analysisExecutor.trackSourceFile(Boolean.parseBoolean(track));
        }
        String write = ns.getString("write");
        if (write != null) {
            analysisExecutor.writeOutput(Boolean.parseBoolean(write));
        }

        String ouput = ns.getString("output");
        if (ouput != null) {
            analysisExecutor.setOutput(ouput);
        }
        String callgraph = ns.getString("callgraph");
        if (callgraph != null) {
            analysisExecutor.setCallGraphAlgorithm(callgraph);
        }
        String timeout = ns.getString("timeout");
        if (timeout != null) {
            analysisExecutor.setTimeout(Integer.parseInt(timeout));
        }

        List<String> es = ns.getList("entryselector");
        if (es != null && !es.isEmpty()) {
            analysisExecutor.setEntrySelector(String.join(",", es));
        }

        List<String> pc = ns.getList("pathchecker");
        if (pc != null && !pc.isEmpty()) {
            analysisExecutor.setPathChecker(String.join(",", pc));
        }

        List<String> rules = ns.getList("rules");
        if (rules != null && !rules.isEmpty()) {
            analysisExecutor.setRules(rules);
        }


        List<String> pl = ns.getList("project");
        if (pl == null || pl.isEmpty()) {
            System.err.println("project is null!");
            System.exit(65);
        }
        boolean multipleOutput = pl.size() > 1 && Boolean.parseBoolean(write);

        if (!multipleOutput) {
            pl.forEach(p -> {
                analysisExecutor.setProject(p);
                analysisExecutor.analysis();
            });
        } else {
            var results = new HashMap<>();
            pl.forEach(p -> {
                analysisExecutor.writeOutput(false);
                analysisExecutor.setProject(p);
                analysisExecutor.analysis();
                var result = analysisExecutor.getRuleResult();
                results.put(p, result);
            });
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                String json = gson.toJson(results);
                Writer writer = new FileWriter(analysisExecutor.getOutput());
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

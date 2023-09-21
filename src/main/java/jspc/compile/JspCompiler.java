package jspc.compile;

import org.apache.jasper.JasperException;
import org.apache.jasper.JspC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JspCompiler implements Compiler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void compile(String uriRoot, String outputDir) {
        logger.info("start jsp compile: " + uriRoot);
        JspC jspc = new JspC();
        jspc.setUriroot(uriRoot);
        jspc.setOutputDir(outputDir);
        jspc.setValidateXml(false);
        jspc.setFailOnError(false);
        jspc.setCompile(true);
        jspc.setSmapDumped(false);
        jspc.setSmapSuppressed(false);
        jspc.setIgnoreJspFragmentErrors(true);
        try {
            jspc.execute();
        } catch (JasperException e) {
            logger.info(e.getMessage());
        }
        logger.info("jsp compile complete.");
    }
}
